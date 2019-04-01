package pers.zc.activiti;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.history.HistoricActivityInstance;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Comment;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.*;

@RunWith(SpringRunner.class)
@SpringBootTest
public class TestBagOrderCheck {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessEngine processEngine;

    private String processId = "187505";
    //二号成本审核key=>(类型：主键：状态)
    private String businessKey = "SQ:100860001:1";

    private Integer taskId;

    @Test
    public void deploy() {
        //bpmn文件部署
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("TestBagOrderCheck.bpmn").name("TestBagOrderCheck").deploy();
        //启动流程
        // 设置User Task1受理人变量
        Map<String, Object> variable = new HashMap<>();
        String [] candidateUsers={"Tec.A","Tec.B","Tec.C","Tec.D"};
        List<String> stringList = Arrays.asList(candidateUsers);
        variable.put("technology", stringList);
        //注：businessKey=>报价单主键:修改码
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("testBagOrderCheck", businessKey, variable);
        processId = processInstance.getId();

        String name = processInstance.getName();
        System.out.println("[deploy]流程创建成功，当前流程实例ID：" + processId);
        System.out.println("流程创建成功，当前流程实例NAME：" + name);

        //任务逻辑，技术部门
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                //.processInstanceId(processId)
                .processInstanceBusinessKey(businessKey)
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + candidateUsers);
        }

        //任务逻辑
        // 设置User Task1受理人变量
        Map<String, Object> variableB = new HashMap<>();
        //业务
        String [] candidateUsersSale={"Sal.A","Sal.B","Sal.C","Sal.D"};
        variableB.put("sale", Arrays.asList(candidateUsersSale));
        //财务
        String [] candidateUsersAccount={"Acc.A","Acc.B","Acc.C","Acc.D"};
        variableB.put("account", Arrays.asList(candidateUsersAccount));
        //生产
        String [] candidateUsersProduct={"Pro.A","Pro.B","Pro.C","Pro.D"};
        variableB.put("product", Arrays.asList(candidateUsersProduct));

        Task task = taskList.get(0);
        taskService.complete(taskList.get(0).getId(),variableB);
        System.out.println("[TaskA]任务完成，任务传给：并行网关");
    }



    @Test
    public void taskB1() {
        System.out.println("[taskB1]流程创建成功，当前流程实例ID：" + processId);
        //任务逻辑
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .taskCandidateUser("Sal.A")
                //.processInstanceId(processId)
                //.taskAssignee("Jay.B")
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getTaskLocalVariables());
        }

        // 由于流程用户上下文对象是线程独立的，所以要在需要的位置设置，要保证设置和获取操作在同一个线程中
        Authentication.setAuthenticatedUserId("Sal.A");//批注人的名称  一定要写，不然查看的时候不知道人物信息
        // 添加批注信息
        taskService.addComment(taskList.get(0).getId(), null, "1:"+"B1完成批注信息");//comment为批注内容*/

        //任务完成，向下执行
        taskService.complete(taskList.get(0).getId());
        System.out.println("[taskB1]任务完成");
    }

    @Test
    public void taskB2() {
        System.out.println("[taskB2]流程创建成功，当前流程实例ID：" + processId);
        //任务逻辑
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .taskCandidateUser("Acc.A")
                //.processInstanceId(processId)
                //.taskAssignee("Jay.B")
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getTaskLocalVariables());
        }

        // 由于流程用户上下文对象是线程独立的，所以要在需要的位置设置，要保证设置和获取操作在同一个线程中
        Authentication.setAuthenticatedUserId("Acc.A");//批注人的名称  一定要写，不然查看的时候不知道人物信息
        // 添加批注信息
        taskService.addComment(taskList.get(0).getId(), null, "2:"+"B2完成批注信息");//comment为批注内容*/

        //任务完成，向下执行
        taskService.complete(taskList.get(0).getId());
        System.out.println("[taskB2]任务完成");
    }

    @Test
    public void taskB3() {
        System.out.println("[taskB3]流程创建成功，当前流程实例ID：" + processId);
        //任务逻辑
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                .processInstanceBusinessKey(businessKey)
                .taskCandidateUser("Pro.A")
                //.processInstanceId(processId)
                //.taskAssignee("Jay.B")
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getTaskLocalVariables());
        }

        // 由于流程用户上下文对象是线程独立的，所以要在需要的位置设置，要保证设置和获取操作在同一个线程中
        Authentication.setAuthenticatedUserId("Pro.A");//批注人的名称  一定要写，不然查看的时候不知道人物信息
        // 添加批注信息
        taskService.addComment(taskList.get(0).getId(), null, "3:"+"B3完成批注信息");//comment为批注内容*/

        //任务完成，向下执行
        taskService.complete(taskList.get(0).getId());
        System.out.println("[taskB3]任务完成");
    }





    /**
     * 相关查询
     */

    //当人任务查询
    @Test
    public void query() {
        System.out.println("[query]当前流程实例ID：" + processId);
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                //.taskCandidateUser("Acc.B2")
                .processInstanceId(processId)
                .list();
        //任务逻辑
        if (taskList.size() != 0) {
            System.out.println("任务查询成功，任务列表：" + taskList);
            for (Task task : taskList) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务执行人：" + task.getAssignee());
            }
        } else {
            System.out.println("流程不存在或已经执行完成");
        }
    }

    //查询批注
    @Test
    public void getProcessComments() {
        List<Comment> historyCommnets = new ArrayList<>();
        //场景类型
        /*String key = "test%";
        List<ProcessDefinition> list = repositoryService
                .createProcessDefinitionQuery() // 创建流程定义查询
                .latestVersion() // 只查询最新版本
                .processDefinitionKeyLike(key)// 根据Key进行模糊查询
                .list(); // 返回列表
        System.out.println(list.size());*/

        //1) 获取流程实例的ID
        ProcessInstance pi = runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                //.processInstanceId(processId)
                .singleResult();
        //2）通过流程实例查询所有的(用户任务类型)历史活动
        List<HistoricActivityInstance> hais = processEngine.getHistoryService().createHistoricActivityInstanceQuery()
                .processInstanceId(pi.getId())
                //.processDefinitionId("test")
                //.activityId("test")
                //.activityType("userTask")
                .list();
        //3）查询每个历史任务的批注
        for (HistoricActivityInstance hai : hais) {
            String historytaskId = hai.getTaskId();
            List<Comment> comments = taskService.getTaskComments(historytaskId);
            // 4）如果当前任务有批注信息，添加到集合中
            if (comments != null && comments.size() > 0) {
                historyCommnets.addAll(comments);
            }
        }
        //5）返回
        for (Comment comment : historyCommnets) {
            System.out.println(comment.getUserId() + ":" + comment.getFullMessage());
        }
        System.out.println(JSONArray.toJSON(historyCommnets));
    }


    public void taskQuery(){
        TaskQuery taskQuery = taskService.createTaskQuery().taskId("102510");
        System.out.println(taskQuery);
    }

    /**
     * 设置流程变量数据
     */
    @Test
    public void setVariableValues(){
        RuntimeService service = processEngine.getRuntimeService();
        List<ProcessInstance> processInstanceList = service.createProcessInstanceQuery()
                .processInstanceBusinessKey(businessKey)
                .list();
        service.setVariable( processId, "checkStatus",3);
        Map<String, Object> objectMap = processInstanceList.get(0).getProcessVariables();
        System.out.println(JSONObject.toJSON(objectMap));
    }

    /**
     * 获取流程变量数据
     */
    @Test
    public void getVariableValues(){
        TaskService taskService=processEngine.getTaskService(); // 任务Service
        Integer count=(Integer) taskService.getVariable(businessKey, "checkStatus");

        System.out.println("数量："+count);
    }
}
