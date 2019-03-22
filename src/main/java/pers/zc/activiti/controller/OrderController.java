package pers.zc.activiti.controller;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import pers.zc.activiti.service.DoService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(value = "order")
public class OrderController {
    public static String processId="70005";
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessEngine processEngine;
    @Autowired
    private DoService doService;

    @RequestMapping(value = "deploy", method = RequestMethod.POST)
    public void deploy(){
        //bpmn文件部署
        repositoryService.createDeployment().addClasspathResource("BagCostCheck.bpmn").name("BagCostCheck").deploy();
        //启动流程
        // 设置User Task1受理人变量
        Map<String, Object> variable = new HashMap<>();
        variable.put("user", "jayce.A");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("bagCostCheck",variable);
        processId=processInstance.getId();
        String name=processInstance.getName();
        System.out.println("[deploy]流程创建成功，当前流程实例ID：" + processId);
        System.out.println("流程创建成功，当前流程实例NAME：" + name);
        //任务逻辑
        List<Task> taskList = taskService.createTaskQuery().processInstanceId(processId).list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }
        //任务完成，向下执行
        Map<String, Object> variables = new HashMap<>();
        variables.put("orgA", "jayce.B");
        variables.put("orgB", "jayce.C");
        variables.put("orgC", "jayce.D");
        taskService.complete(taskList.get(0).getId(),variables);
        System.out.println("[订单存在]任务完成，任务并行传给：jayce.B_C_D");
    }

    @RequestMapping(value = "query", method = RequestMethod.POST)
    public void query(){
        System.out.println("[query]当前流程实例ID：" + processId);
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                //.taskAssignee("jayce.B")
                .processInstanceId(processId)
                .list();
        //任务逻辑
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }

        //任务完成，向下执行
        /*Map<String, Object> variables = new HashMap<>();
        variables.put("user", "sun jie");
        taskService.complete(taskList.get(0).getId(),variables);*/
    }

    @RequestMapping(value = "checkA", method = RequestMethod.POST)
    public void checkA(){
        System.out.println("[checkA]当前流程实例ID：" + processId);
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                //财务审核
                .taskAssignee("jayce.B")
                .processInstanceId(processId)
                .list();
        //任务逻辑
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }
        //A任务完成，向下执行
        taskService.complete(taskList.get(0).getId());
    }

    @RequestMapping(value = "revokeB", method = RequestMethod.POST)
    public void revokeB() throws Exception {
        System.out.println("[revokeB]当前流程实例ID：" + processId);
        doService.revoke(processId,"jayce.B");
    }


}
