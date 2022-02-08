package io.metersphere;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import io.metersphere.api.dto.ApiTestImportRequest;
import io.metersphere.api.dto.automation.parse.ScenarioImport;
import io.metersphere.api.dto.automation.parse.ScenarioImportParserFactory;
import io.metersphere.api.dto.definition.request.MsScenario;
import io.metersphere.api.dto.definition.request.sampler.MsHTTPSamplerProxy;
import io.metersphere.api.dto.parse.postman.PostmanCollection;
import io.metersphere.api.dto.parse.postman.PostmanCollectionInfo;
import io.metersphere.api.dto.parse.postman.PostmanItem;
import io.metersphere.api.dto.parse.postman.PostmanKeyValue;
import io.metersphere.api.service.ApiAutomationService;
import io.metersphere.api.service.ApiScenarioModuleService;
import io.metersphere.base.domain.ApiScenarioWithBLOBs;
import io.metersphere.commons.exception.MSException;
import io.metersphere.commons.utils.LogUtil;
import io.metersphere.plugin.core.MsTestElement;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class Test{

    @Resource
    ApiScenarioModuleService apiScenarioModuleService;
    @Resource
    private ApiAutomationService apiAutomationService;

    @org.junit.Test
    public void name() throws FileNotFoundException {
        System.out.println("hehhe");
        //apiAutomationService.scenarioImport(file, request);
        //apiScenarioModuleService.addNode(node);
        //File file = new File("/opt/metersphere/农银金科.postman_collection.json");
        InputStream fos = new FileInputStream("/opt/metersphere/农银金科.postman_collection.json");
        String testStr = getApiTestStr(fos);
        PostmanCollection postmanCollection = JSON.parseObject(testStr, PostmanCollection.class);
        List<PostmanKeyValue> variables = postmanCollection.getVariable();

        // 场景步骤
        LinkedList<MsTestElement> apiScenarioWithBLOBs = new LinkedList<>();
        PostmanCollectionInfo info = postmanCollection.getInfo();
        ApiScenarioWithBLOBs scenario = new ApiScenarioWithBLOBs();
        scenario.setName(info.getName());

        MsScenario msScenario = new MsScenario();
        msScenario.setName(info.getName());
        //this.projectId = request.getProjectId();
        ScenarioImport scenarioImport = new ScenarioImport();

        ApiScenarioWithBLOBs apiScenarioWithBLOBs1 = new ApiScenarioWithBLOBs();
        apiScenarioWithBLOBs1.setModulePath("/dev");
        apiScenarioWithBLOBs1.setApiScenarioModuleId("47ba7690-1424-4153-9916-016de5b929cb");
        apiScenarioWithBLOBs1.setProjectId("fa34f2dd-c959-4f29-bd38-64acfc701fce");
        apiScenarioWithBLOBs1.setUserId("kaibo");
        parseItem(postmanCollection,apiScenarioWithBLOBs1,apiScenarioWithBLOBs1.getProjectId(),apiScenarioWithBLOBs1.getUserId());


        System.out.println();
    }

    protected String getApiTestStr(InputStream source) {
        StringBuilder testStr = null;
        try (BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(source, StandardCharsets.UTF_8))) {
            testStr = new StringBuilder();
            String inputStr;
            while ((inputStr = bufferedReader.readLine()) != null) {
                testStr.append(inputStr);
            }
        } catch (Exception e) {
            MSException.throwException(e.getMessage());
            LogUtil.error(e.getMessage(), e);
        } finally {
            try {
                if (source != null) {
                    source.close();
                }
            } catch (IOException e) {
                MSException.throwException(e.getMessage());
                LogUtil.error(e.getMessage(), e);
            }
        }
        return testStr.toString();
    }

    private void parseItem(PostmanCollection postmanCollection,ApiScenarioWithBLOBs apiScenario,String projectId,String userId) throws FileNotFoundException {
        List<PostmanCollection> postmanCollectionChildList = new ArrayList<>();
        Iterator<PostmanItem> it = postmanCollection.getItem().iterator();
        while(it.hasNext()){
            PostmanItem item = it.next();
            List<PostmanItem> childItems = item.getItem();
            if (childItems != null) {
                it.remove();
                PostmanCollection postmanCollectionChild = new PostmanCollection();
                postmanCollectionChild.setItem(childItems);
                PostmanCollectionInfo infoChild = new PostmanCollectionInfo();
                infoChild.setName(item.getName());
                infoChild.setPostmanId(UUID.randomUUID().toString());
                postmanCollectionChild.setInfo(infoChild);
                postmanCollectionChildList.add(postmanCollectionChild);
                //parseItem(postmanCollectionChild, variables, scenario, results);
            }
        }
        JSONObject jsonObject = (JSONObject) JSONObject.toJSON(postmanCollection);
        System.out.println(jsonObject);

        String jsonString1 = jsonObject.toString();
        CreateFileUtil.createJsonFile(jsonString1, "/opt/metersphere/", postmanCollection.getInfo().getName());

        InputStream fos = new FileInputStream("/opt/metersphere/"+postmanCollection.getInfo().getName()+".json");

        ApiTestImportRequest request = new ApiTestImportRequest();
        request.setPlatform("Postman");
        request.setModuleId(apiScenario.getApiScenarioModuleId());
        request.setModulePath(apiScenario.getModulePath());
        request.setProjectId(projectId);
        request.setUserId(userId);
        //request.set
        //执行导入逻辑
        ScenarioImport scenarioImport = apiAutomationService.scenarioImport2(fos, request);
        //
        ApiScenarioWithBLOBs scenario1 = scenarioImport.getData().get(0);

        //递归调用
        for(PostmanCollection postmanCollection1 : postmanCollectionChildList){
            parseItem(postmanCollection1,scenario1,projectId,userId);
        }

    }
}


