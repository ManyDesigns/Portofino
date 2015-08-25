import com.manydesigns.elements.util.MimeTypes
import org.json.JSONArray
import org.json.JSONObject

import javax.servlet.*

import com.manydesigns.elements.messages.*
import com.manydesigns.elements.reflection.*
import com.manydesigns.portofino.*
import com.manydesigns.portofino.buttons.*
import com.manydesigns.portofino.buttons.annotations.*
import com.manydesigns.portofino.dispatcher.*
import com.manydesigns.portofino.model.database.*
import com.manydesigns.portofino.pageactions.*
import com.manydesigns.portofino.security.*
import com.manydesigns.portofino.shiro.*

import net.sourceforge.stripes.action.*
import org.apache.shiro.*
import org.hibernate.*
import org.hibernate.criterion.*

import com.manydesigns.portofino.pageactions.custom.*

@RequiresPermissions(level = AccessLevel.VIEW)
class MyCustomAction extends CustomAction {

    //Automatically generated on Mon Aug 24 16:25:00 CEST 2015 by ManyDesigns Portofino
    //Write your code here

    @DefaultHandler
    public Resolution execute() {
        String fwd = "/WEB-INF/pages/example/fancytree/fancytree.jsp";
        return new ForwardResolution(fwd);
    }

    public Resolution ajaxSampleData(){
        def jsonArray = new JSONArray()
        def ob1 = createObject("Animalia", true, true, true)
        def ob2 = createObject("Chordate", false, true, true)
        def ob3 = createObject("Mammal", false, true, true)
        def ob4 = createObject("Primate", false, false, false)
        def ob5 = createObject("Carnivora", false, false, false)
        def ob6 = createObject("Felidae", false, false, false)
        def ob7 = createObject("Arthropoda", true, true, true)
        def ob8 = createObject("Insect", false, true, true)
        def ob9 = createObject("Diptera", false, false, false)

        jsonArray.put(ob1)
        def children1 = new JSONArray()
        children1.put(ob2)
        children1.put(ob7)
        ob1.put("children", children1)

        def children2 = new JSONArray()
        children2.put(ob3)
        ob2.put("children", children2)

        def children3 = new JSONArray()
        children3.put(ob4)
        children3.put(ob5)
        ob3.put("children", children3)

        def children4 = new JSONArray()
        children4.put(ob6)
        ob5.put("children", children4)

        def children5 = new JSONArray()
        children5.put(ob8)
        ob7.put("children", children5)

        def children6 = new JSONArray()
        children6.put(ob9)
        ob8.put("children", children6)
        ob9.put("children", new JSONArray())


        //jsonArray = new JSONArray(result)
        return  new StreamingResolution(MimeTypes.APPLICATION_JSON_UTF8, jsonArray.toString())
    }

    private JSONObject createObject(String title, boolean expanded, boolean folder, boolean lazy){
        Map map = new HashMap()
        map.title = title
        map.expanded = expanded
        map.folder = folder
        map.lazy = lazy

        return new JSONObject(map)
    }
}