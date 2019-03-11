package com.manydesigns.portofino.upstairs.actions;

import com.manydesigns.elements.messages.RequestMessages;
import com.manydesigns.elements.ognl.OgnlUtils;
import com.manydesigns.elements.util.Util;
import com.manydesigns.portofino.PortofinoProperties;
import com.manydesigns.portofino.code.CodeBase;
import com.manydesigns.portofino.model.database.Database;
import com.manydesigns.portofino.model.database.DatabaseLogic;
import com.manydesigns.portofino.model.database.Table;
import com.manydesigns.portofino.modules.Module;
import com.manydesigns.portofino.pageactions.AbstractPageAction;
import com.manydesigns.portofino.persistence.Persistence;
import com.manydesigns.portofino.security.RequiresAdministrator;
import com.manydesigns.portofino.upstairs.ModuleInfo;
import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;
import groovy.text.TemplateEngine;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.io.DefaultResourceLoader;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.manydesigns.portofino.spring.PortofinoSpringConfiguration.ACTIONS_DIRECTORY;

/**
 * @author Emanuele Poggi       - emanuele.poggi@manydesigns.com
 * @author Angelo Lupo          - angelo.lupo@manydesigns.com
 * @author Giampiero Granatella - giampiero.granatella@manydesigns.com
 * @author Alessio Stalla       - alessio.stalla@manydesigns.com
 */
@RequiresAuthentication
@RequiresAdministrator
public class UpstairsAction extends AbstractPageAction {
    public static final String copyright = "Copyright (C) 2005-2017 ManyDesigns srl";

    public final static Logger logger = LoggerFactory.getLogger(UpstairsAction.class);

    @Autowired
    ApplicationContext applicationContext;

    @Autowired
    CodeBase codeBase;

    @Autowired
    Persistence persistence;

    @Autowired
    @Qualifier(ACTIONS_DIRECTORY)
    File actionsDirectory;

    @SuppressWarnings({"RedundantStringConstructorCall"})
    public static final String NO_LINK_TO_PARENT = new String();
    public static final int LARGE_RESULT_SET_THRESHOLD = 10000;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> getInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("version", PortofinoProperties.getPortofinoVersion());
        List<ModuleInfo> modules = new ArrayList<>();
        for(Module module : applicationContext.getBeansOfType(Module.class).values()) {
            ModuleInfo view = new ModuleInfo();
            view.moduleClass = module.getClass().getName();
            view.name = module.getName();
            view.status = module.getStatus().name();
            view.version = module.getModuleVersion();
            modules.add(view);
        }
        info.put("modules", modules);
        return info;
    }

    @POST
    @Path("restart")
    public void restartApplication() throws Exception {
        codeBase.clear();
        OgnlUtils.clearCache();
        if(applicationContext instanceof ConfigurableApplicationContext) {
            //Spring enhances @Configuration classes. To do so it loads them by name from its classloader.
            //Thus, replacing the classloader with a fresh one has also the side-effect of making Spring reload the user
            //SpringConfiguration class, provided it already existed and was annotated with @Configuration.
            //Note that Spring won't pick up new @Bean methods. It will also barf badly on removed @Bean methods,
            //effectively crashing the application. Changing the return value and even the return type is fine.
            ((DefaultResourceLoader) applicationContext).setClassLoader(codeBase.asClassLoader());
            ((ConfigurableApplicationContext) applicationContext).refresh();
        }
    }

    @POST
    @Path("application")
    public void createApplication(Map wizard) throws Exception {
        String strategy = (String) wizard.get("strategy");
        switch (strategy) {
            case "automatic":
            case "manual":
                String databaseName = (String) ((Map) wizard.get("connectionProvider")).get("name");
                List<Map> tables = (List) wizard.get("tables");
                Database database = DatabaseLogic.findDatabaseByName(persistence.getModel(), databaseName);
                if(database == null) {
                    throw new WebApplicationException("The database does not exist: " + databaseName);
                }
                TemplateEngine engine = new SimpleTemplateEngine();
                Template template = engine.createTemplate(UpstairsAction.class.getResource("/com/manydesigns/portofino/upstairs/wizard/CrudPage.groovy"));
                for(Map tableInfo : tables) {
                    if((Boolean) tableInfo.get("selected")) {
                        Map tableMap = (Map) tableInfo.get("table");
                        String tableName = (String) tableMap.get("tableName");
                        Table table = DatabaseLogic.findTableByName(persistence.getModel(), databaseName, (String) tableMap.get("schemaName"), tableName);
                        if(table == null) {
                            logger.warn("Table not found: {}", tableMap);
                            RequestMessages.addErrorMessage("Table not found: " + tableName);
                            continue;
                        }
                        File dir = new File(actionsDirectory, table.getActualEntityName());
                        //depth = 1;
                        //createCrudPage(dir, table, template);
                    }
                }
                /*if(userTable != null) {
                    setupUserPages(childPages, template);
                }*/
                break;
            case "none":
                break;
            default:
                throw new WebApplicationException("Invalid strategy: " + strategy);
        }
    }

    /*protected void createCrudPage(File dir, Table table, List<ChildPage> childPages, Template template)
            throws Exception {
        String query = "from " + table.getActualEntityName() + " order by id desc";
        String title = Util.guessToWords(table.getActualEntityName());
        HashMap<String, String> bindings = new HashMap<String, String>();
        bindings.put("parentName", "");
        bindings.put("parentProperty", "nothing");
        bindings.put("linkToParentProperty", NO_LINK_TO_PARENT);
        createCrudPage(dir, table, query, childPages, template, bindings, title);
    }

    protected void createCrudPage(
            File dir, Table table, String query, List<ChildPage> childPages,
            Template template, Map<String, String> bindings, String title)
            throws Exception {
        if(dir.exists()) {
            SessionMessages.addWarningMessage(
                    ElementsThreadLocals.getText("directory.exists.page.not.created._", dir.getAbsolutePath()));
            return null;
        } else if(dir.mkdirs()) {
            logger.info("Creating CRUD page {}", dir.getAbsolutePath());
            CrudConfiguration configuration = new CrudConfiguration();
            configuration.setDatabase(connectionProvider.getDatabase().getDatabaseName());
            configuration.setupDefaults();

            configuration.setQuery(query);
            String variable = table.getActualEntityName();
            configuration.setVariable(variable);
            detectLargeResultSet(table, configuration);

            configuration.setName(table.getActualEntityName());

            int summ = 0;
            String linkToParentProperty = bindings.get("linkToParentProperty");
            for(Column column : table.getColumns()) {
                summ = setupColumn(column, configuration, summ, linkToParentProperty);
            }

            DispatcherLogic.saveConfiguration(dir, configuration);
            Page page = new Page();
            page.setId(RandomUtil.createRandomId());
            page.setTitle(title);
            page.setDescription(title);

            Collection<Reference> references = children.get(table);
            if(references != null && depth < maxDepth) {
                ArrayList<ChildPage> pages = page.getDetailLayout().getChildPages();
                depth++;
                for(Reference ref : references) {
                    createChildCrudPage(dir, template, variable, references, ref, pages);
                }
                depth--;
                Collections.sort(pages, new Comparator<ChildPage>() {
                    public int compare(ChildPage o1, ChildPage o2) {
                        return o1.getName().compareToIgnoreCase(o2.getName());
                    }
                });
            }

            DispatcherLogic.savePage(dir, page);
            File actionFile = new File(dir, "action.groovy");
            FileWriter fileWriter = new FileWriter(actionFile);

            template.make(bindings).writeTo(fileWriter);
            IOUtils.closeQuietly(fileWriter);

            logger.debug("Creating _detail directory");
            File detailDir = new File(dir, PageInstance.DETAIL);
            if(!detailDir.isDirectory() && !detailDir.mkdir()) {
                logger.warn("Could not create detail directory {}", detailDir.getAbsolutePath());
                SessionMessages.addWarningMessage(
                        ElementsThreadLocals.getText("couldnt.create.directory", detailDir.getAbsolutePath()));
            }

            ChildPage childPage = new ChildPage();
            childPage.setName(dir.getName());
            childPage.setShowInNavigation(true);
            childPages.add(childPage);

            return page;
        } else {
            logger.warn("Couldn't create directory {}", dir.getAbsolutePath());
            SessionMessages.addWarningMessage(
                    ElementsThreadLocals.getText("couldnt.create.directory", dir.getAbsolutePath()));
            return null;
        }
    }*/

}
