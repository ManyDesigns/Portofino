import com.manydesigns.elements.util.MimeTypes

import javax.servlet.*
import javax.ws.rs.*

import com.manydesigns.elements.messages.*
import com.manydesigns.elements.reflection.*
import com.manydesigns.elements.util.MimeTypes
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
class FancyTreeExample extends CustomAction {

    //Automatically generated on Mon Aug 24 16:25:00 CEST 2015 by ManyDesigns Portofino
    //Write your code here

    @DefaultHandler
    public Resolution execute() {
        String fwd = "/WEB-INF/pages/samples/fancytree/fancytree.jsp";
        return new ForwardResolution(fwd);
    }

    @GET
    @Produces(MimeTypes.APPLICATION_JSON_UTF8)
    @Path("data")
    def ajaxSampleData(){
        def ob1 = createObject("Animalia", true, true, true)
        def ob2 = createObject("Chordate", false, true, true)
        def ob3 = createObject("Mammal", false, true, true)
        def ob4 = createObject("Primate", false, false, false)
        def ob5 = createObject("Carnivora", false, false, false)
        def ob6 = createObject("Felidae", false, false, false)
        def ob7 = createObject("Arthropoda", true, true, true)
        def ob8 = createObject("Insect", false, true, true)
        def ob9 = createObject("Diptera", false, false, false)
        ob1.children = [ob2, ob7]
        ob2.children = [ob3]
        ob3.children = [ob4, ob5]
        ob5.children = [ob6]
        ob7.children = [ob8]
        ob8.children = [ob9]
        ob9.children = []
        [ob1]
    }

    private Map createObject(String title, boolean expanded, boolean folder, boolean lazy){
        [title: title, expanded: expanded, folder: folder, lazy: lazy]
    }
}