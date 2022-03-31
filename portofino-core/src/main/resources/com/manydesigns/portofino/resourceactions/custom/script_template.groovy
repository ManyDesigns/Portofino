package com.manydesigns.portofino.resourceactions.custom

import javax.servlet.*

import com.manydesigns.elements.messages.*
import com.manydesigns.elements.reflection.*
import com.manydesigns.portofino.*
import com.manydesigns.portofino.dispatcher.*
import com.manydesigns.portofino.database.model.*
import com.manydesigns.portofino.resourceactions.*
import com.manydesigns.portofino.security.*
import com.manydesigns.portofino.shiro.*

import org.apache.shiro.*
import org.hibernate.*
import org.hibernate.criterion.*

import com.manydesigns.portofino.resourceactions.custom.*

import javax.ws.rs.GET

@RequiresPermissions(level = AccessLevel.VIEW)
class %{#generatedClassName} extends CustomAction {

    //Automatically generated on %{new java.util.Date()} by ManyDesigns Portofino
    //Write your code here

    @GET
    public String greet() {
        "Hello, it works!"
    }

}
