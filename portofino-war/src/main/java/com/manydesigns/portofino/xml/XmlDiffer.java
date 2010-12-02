/*
 * Copyright (C) 2005-2010 ManyDesigns srl.  All rights reserved.
 * http://www.manydesigns.com/
 *
 * Unless you have purchased a commercial license agreement from ManyDesigns srl,
 * the following license terms apply:
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License version 3 as published by
 * the Free Software Foundation.
 *
 * There are special exceptions to the terms and conditions of the GPL
 * as it is applied to this software. View the full text of the
 * exception in file OPEN-SOURCE-LICENSE.txt in the directory of this
 * software distribution.
 *
 * This program is distributed WITHOUT ANY WARRANTY; and without the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, see http://www.gnu.org/licenses/gpl.txt
 * or write to:
 * Free Software Foundation, Inc.,
 * 59 Temple Place - Suite 330,
 * Boston, MA  02111-1307  USA
 *
 */

package com.manydesigns.portofino.xml;

import com.manydesigns.elements.reflection.ClassAccessor;
import com.manydesigns.elements.reflection.JavaClassAccessor;
import com.manydesigns.elements.reflection.PropertyAccessor;
import com.manydesigns.elements.util.Util;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/*
* @author Paolo Predonzani     - paolo.predonzani@manydesigns.com
* @author Angelo Lupo          - angelo.lupo@manydesigns.com
* @author Giampiero Granatella - giampiero.granatella@manydesigns.com
*/
public class XmlDiffer {
    public static final String copyright =
            "Copyright (c) 2005-2010, ManyDesigns srl";
    
    //--------------------------------------------------------------------------
    // Constants
    //--------------------------------------------------------------------------

    public enum Status {
        BOTH_NULL,
        SOURCE_NULL,
        TARGET_NULL,
        EQUAL,
        DIFFERENT
    }

    //--------------------------------------------------------------------------
    // Logging
    //--------------------------------------------------------------------------

    public static final Logger logger =
            LoggerFactory.getLogger(XmlWriter.class);

    //--------------------------------------------------------------------------
    // Constructor
    //--------------------------------------------------------------------------

    public XmlDiffer() {}


    //--------------------------------------------------------------------------
    // Methods
    //--------------------------------------------------------------------------


    public ElementDiffer diff(String elementName, Object sourceRoot, Object targetRoot) {
        ElementDiffer rootDiffer = new ElementDiffer(elementName, sourceRoot, targetRoot);
        rootDiffer.diff();
        return rootDiffer;
    }

    private Object[] extractIdentifier(PropertyAccessor[] identifierProperties, Object obj) {
        Object[] result = new Object[identifierProperties.length];
        for (int i = 0; i < identifierProperties.length; i++) {
            result[i] =  identifierProperties[i].get(obj);
        }
        return result;
    }

    private PropertyAccessor[] getIdentifierProperties(ClassAccessor classAccessor) {
        List<PropertyAccessor> properties = new ArrayList<PropertyAccessor>();
        for (PropertyAccessor current : classAccessor.getProperties()) {
            XmlAttribute xmlAttribute =
                    current.getAnnotation(XmlAttribute.class);
            if (xmlAttribute == null) {
                continue;
            }

            if (xmlAttribute.identifier()) {
                properties.add(current);
            }
        }
        PropertyAccessor[] result = new PropertyAccessor[properties.size()];
        properties.toArray(result);
        return result;
    }



    //--------------------------------------------------------------------------
    // ElementDiffer
    //--------------------------------------------------------------------------

    public interface Differ {
        String getName();
        String getType();
        Status getStatus();
        List<Differ> getChildDiffers();
    }

    //--------------------------------------------------------------------------
    // ElementDiffer
    //--------------------------------------------------------------------------

    public class ElementDiffer implements Differ {
        final String elementName;
        final Object sourceElement;
        final Object targetElement;

        final Class sourceClass;
        final Class targetClass;

        ClassAccessor commonClassAccessor;
        PropertyAccessor[] identifierProperties;

        final List<AttributeDiffer> attributeDiffers;
        final List<Differ> childDiffers;

        Status status;

        ElementDiffer(String elementName,
                      Object sourceElement,
                      Object targetElement) {
            this.elementName = elementName;
            this.sourceElement = sourceElement;
            this.targetElement = targetElement;

            if (sourceElement == null) {
                sourceClass = null;
            } else {
                sourceClass = sourceElement.getClass();
                commonClassAccessor =
                        JavaClassAccessor.getClassAccessor(sourceClass);
            }

            if (targetElement == null) {
                targetClass = null;
            } else {
                targetClass = targetElement.getClass();
                commonClassAccessor =
                        JavaClassAccessor.getClassAccessor(targetClass);
            }

            identifierProperties = getIdentifierProperties(commonClassAccessor);

            if (sourceElement == null) {
                if (targetElement == null) {
                    status = Status.BOTH_NULL;
                } else {
                    status = Status.SOURCE_NULL;
                }
            } else {
                if (targetElement == null) {
                    status = Status.TARGET_NULL;
                } else {
                    status = Status.EQUAL;
                }
            }

            attributeDiffers = new ArrayList<AttributeDiffer>();
            childDiffers = new ArrayList<Differ>();
        }

        public void diff() {
            if (sourceElement != null && targetElement != null) {
                if (sourceClass != targetClass) {
                    String msg = String.format(
                            "Source/target of different java types: %s/%s",
                            sourceClass.getName(), targetClass.getName());
                    throw new Error(msg);
                }

                diffAttributes();

                XmlCollection ownCollectionAnnotation =
                        commonClassAccessor.getAnnotation(XmlCollection.class);
                if (ownCollectionAnnotation != null) {
                    CollectionDiffer collectionDiffer =
                            new CollectionDiffer(elementName,
                                    childDiffers,
                                    ownCollectionAnnotation,
                                    (List)sourceElement,
                                    (List)targetElement);
                    collectionDiffer.diff();
                }

                for (PropertyAccessor propertyAccessor
                        : commonClassAccessor.getProperties()) {
                    XmlElement elementAnnotation =
                            propertyAccessor.getAnnotation(XmlElement.class);
                    XmlCollection collectionAnnotation =
                            propertyAccessor.getAnnotation(XmlCollection.class);

                    if (elementAnnotation != null) {
                        Object sourceChildElement = propertyAccessor.get(sourceElement);
                        Object targetChildElement = propertyAccessor.get(targetElement);
                        ElementDiffer elementDiffer =
                                new ElementDiffer(
                                        propertyAccessor.getName(),
                                        sourceChildElement,
                                        targetChildElement
                                );
                        elementDiffer.diff();
                        childDiffers.add(elementDiffer);
                    }

                    if (collectionAnnotation != null) {
                        List sourceCollection =
                                (List)propertyAccessor.get(sourceElement);
                        List targetCollection =
                                (List)propertyAccessor.get(targetElement);
                        CollectionDiffer collectionDiffer =
                            new CollectionDiffer(
                                    propertyAccessor.getName(),
                                    collectionAnnotation,
                                    sourceCollection, targetCollection);
                        collectionDiffer.diff();
                        childDiffers.add(collectionDiffer);
                    }
                }
            }
        }

        private void diffAttributes() {
            for (PropertyAccessor propertyAccessor :
                    commonClassAccessor.getProperties()) {
                XmlAttribute xmlAttribute =
                        propertyAccessor.getAnnotation(XmlAttribute.class);
                if (xmlAttribute == null) {
                    continue;
                }

                Object sourceValue = propertyAccessor.get(sourceElement);
                Object targetValue = propertyAccessor.get(targetElement);

                AttributeDiffer attributeDiffer =
                        new AttributeDiffer(propertyAccessor,
                                sourceValue, targetValue);
                attributeDiffers.add(attributeDiffer);
            }
        }

        public String getName() {
            Object[] identifier1;
            Object[] identifier2;
            switch(status) {
                case BOTH_NULL:
                    return null;
                case SOURCE_NULL:
                    identifier1 = extractIdentifier(
                            identifierProperties, targetElement);
                    return concat(identifier1);
                case TARGET_NULL:
                    identifier2 = extractIdentifier(
                            identifierProperties, sourceElement);
                    return concat(identifier2);
                case EQUAL:
                case DIFFERENT:
                    identifier1 = extractIdentifier(
                            identifierProperties, targetElement);
                    return concat(identifier1);
                default:
                    logger.error("Unknown case");
                    throw new Error("Unknown case");
            }
        }

        String concat(Object[] array) {
            return concat(array, " ");
        }

        String concat(Object[] array, String separator) {
            StringBuffer sb = new StringBuffer();
            boolean first = true;
            for (Object current : array) {
                if (first) {
                    first = false;
                } else {
                    sb.append(separator);
                }
                String stringValue = Util.convertValueToString(current);
                sb.append(stringValue);
            }
            return sb.toString();
        }

        public String getType() {
            return elementName;
        }

        public Status getStatus() {
            return status;
        }

        public List<Differ> getChildDiffers() {
            return childDiffers;
        }
    }


    //--------------------------------------------------------------------------
    // CollectionDiffer
    //--------------------------------------------------------------------------

    class CollectionDiffer implements Differ {
        final String elementName;
        final List<Differ> childDiffers;
        final XmlCollection collectionAnnotation;
        final List sourceCollection;
        final List targetCollection;
        final Class type;
        final ClassAccessor classAccessor;
        final PropertyAccessor[] identifierProperties;

        Collection<Object[]> sourceIdentifiers;
        Collection<Object[]> targetIdentifiers;
        List<Object[]> allIdentifiers;

        CollectionDiffer(String elementName, XmlCollection collectionAnnotation,
                         List sourceCollection,
                         List targetCollection) {
            this(elementName, new ArrayList<Differ>(), collectionAnnotation,
                    sourceCollection, targetCollection);
        }
        
        CollectionDiffer(String elementName,
                         List<Differ> childDiffers,
                         XmlCollection collectionAnnotation,
                         List sourceCollection,
                         List targetCollection) {
            this.elementName = elementName;
            this.collectionAnnotation = collectionAnnotation;
            this.childDiffers = childDiffers;
            this.sourceCollection = sourceCollection;
            this.targetCollection = targetCollection;

            type = collectionAnnotation.itemClasses()[0];
            classAccessor = JavaClassAccessor.getClassAccessor(type);
            identifierProperties = getIdentifierProperties(classAccessor);
        }

        void diff() {
            String itemName = collectionAnnotation.itemNames()[0];

            if (sourceCollection == null || sourceCollection.isEmpty()) {
                for (Object current : targetCollection) {
                    ElementDiffer currentDiffer =
                            new ElementDiffer(itemName, null, current);
                    childDiffers.add(currentDiffer);
                }
            } else if (targetCollection == null || targetCollection.isEmpty()) {
                for (Object current : sourceCollection) {
                    ElementDiffer currentDiffer =
                            new ElementDiffer(itemName, current, null);
                    childDiffers.add(currentDiffer);
                }
            } else {
                // collect identifiers from source/target collections
                sourceIdentifiers = collectIdentifiers(sourceCollection);
                targetIdentifiers = collectIdentifiers(targetCollection);

                // merge source/target identifiers into allIdentifiers
                allIdentifiers = new ArrayList<Object[]>(sourceIdentifiers);
                for (Object[] identifier : targetIdentifiers) {
                    if (!contains(allIdentifiers, identifier)) {
                        allIdentifiers.add(identifier);
                    }
                }

                for (Object[] identifier : allIdentifiers) {
                    int sourceIndex = indexOf(sourceIdentifiers, identifier);
                    Object sourceChildElement = null;
                    if (sourceIndex >= 0) {
                        sourceChildElement = sourceCollection.get(sourceIndex);
                    }

                    int targetIndex = indexOf(targetIdentifiers, identifier);
                    Object targetChildElement = null;
                    if (targetIndex >= 0) {
                        targetChildElement = targetCollection.get(targetIndex);
                    }

                    ElementDiffer childElementDiffer =
                            new ElementDiffer(itemName, sourceChildElement,
                                    targetChildElement);
                    childElementDiffer.diff();
                    childDiffers.add(childElementDiffer);
                }
            }
        }

        private Collection<Object[]> collectIdentifiers(Collection collection) {
            return (Collection<Object[]>)CollectionUtils.collect(
                    collection, new Transformer() {
                public Object transform(Object input) {
                    return extractIdentifier(identifierProperties, input);
                }
            });
        }

        private boolean contains(List<Object[]> identifierList, Object[] identifier) {
            return indexOf(identifierList, identifier) >= 0;
        }

        private int indexOf(Collection<Object[]> identifiers, Object[] identifier) {
            int index = 0;
            for (Object[] current : identifiers) {
                if (Arrays.equals(current, identifier)) {
                    return index;
                }
                index++;
            }
            return -1;
        }

        public String getName() {
            if (childDiffers.isEmpty()) {
                return elementName + " (none)";
            } else {
                return elementName;
            }
        }

        public String getType() {
            return elementName;
        }

        public Status getStatus() {
            return Status.EQUAL;
        }

        public List<Differ> getChildDiffers() {
            return childDiffers;
        }
    }

    //--------------------------------------------------------------------------
    // AttributeDiffer
    //--------------------------------------------------------------------------

    public static class AttributeDiffer {
        final PropertyAccessor propertyAccessor;
        final Object sourceValue;
        final Object targetValue;

        Status status;

        public AttributeDiffer(PropertyAccessor propertyAccessor,
                               Object sourceValue, Object targetValue) {
            this.propertyAccessor = propertyAccessor;
            this.sourceValue = sourceValue;
            this.targetValue = targetValue;
        }

        public void diff() {
            if (sourceValue == null) {
                if (targetValue == null) {
                    status = Status.BOTH_NULL;
                } else {
                    status = Status.SOURCE_NULL;
                }
            } else {
                if (targetValue == null) {
                    status = Status.TARGET_NULL;
                } else {
                    if (sourceValue.equals(targetValue)) {
                        status = Status.EQUAL;
                    } else {
                        status = Status.DIFFERENT;
                    }
                }
            }
        }

        public PropertyAccessor getPropertyAccessor() {
            return propertyAccessor;
        }

        public Object getSourceValue() {
            return sourceValue;
        }

        public Object getTargetValue() {
            return targetValue;
        }

        public Status getStatus() {
            return status;
        }
    }
}
