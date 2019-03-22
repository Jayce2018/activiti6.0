package pers.zc.activiti.controller;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zc 2018-06-04
 */
@RestController
@RequestMapping("/activiti")
public class ActivitiController {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessEngine processEngine;

    @RequestMapping("helloWorld")
    public void helloWorld() {

        //根据bpmn文件部署流程  
        Deployment deploy = repositoryService.createDeployment()
                .addClasspathResource("TestProcess.bpmn")
                .addClasspathResource("TestProcess.png")
                .deploy();
        //获取流程定义  
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deploy.getId()).singleResult();
        //启动流程定义，返回流程实例  
        ProcessInstance pi = runtimeService.startProcessInstanceById(processDefinition.getId());
        String processId = pi.getId();
        System.out.println("流程创建成功，当前流程实例ID：" + processId);

        Task task = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        System.out.println("执行前，任务名称：" + task.getName());
        taskService.complete(task.getId());

        task = taskService.createTaskQuery().processInstanceId(processId).singleResult();
        System.out.println("task为null，任务执行完毕：" + task);
    }

    @RequestMapping("singleAssignee")
    public void setSingleAssignee() {

        //根据bpmn文件部署流程
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("singleAssignee.bpmn").deploy();
        // 设置User Task1受理人变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("user1", "007");
        //采用key来启动流程定义并设置流程变量，返回流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("singleAssignee", variables);
        String processId = pi.getId();
        System.out.println("流程创建成功，当前流程实例ID：" + processId);

        //查询流程
        List<ProcessDefinition> list1 = processEngine.getRepositoryService()// 与流程定义和部署对象相关的Service
                .createProcessDefinitionQuery()// 创建一个流程定义的查询
                /** 指定查询条件,where条件 */
                .deploymentId(deployment.getId())//使用部署对象ID查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                // .processDefinitionKey(processDefinitionKey)//使用流程定义的key查询
                // .processDefinitionNameLike(processDefinitionNameLike)//使用流程定义的名称模糊查询

                /** 排序 */
                .orderByProcessDefinitionVersion().asc()// 按照版本的升序排列
                // .orderByProcessDefinitionName().desc()//按照流程定义的名称降序排列

                /** 返回的结果集 */
                .list();// 返回一个集合列表，封装流程定义
        // .singleResult();//返回惟一结果集
        // .count();//返回结果集数量
        // .listPage(firstResult, maxResults);//分页查询
        if (list1 != null && list1.size() > 0) {
            for (ProcessDefinition pd : list1) {
                System.out.println("流程定义ID:" + pd.getId());// 流程定义的key+版本+随机生成数
                System.out.println("流程定义的名称:" + pd.getName());// 对应helloworld.bpmn文件中的name属性值
                System.out.println("流程定义的key:" + pd.getKey());// 对应helloworld.bpmn文件中的id属性值
                System.out.println("流程定义的版本:" + pd.getVersion());// 当流程定义的key值相同的相同下，版本升级，默认1
                System.out.println("资源名称bpmn文件:" + pd.getResourceName());
                System.out.println("资源名称png文件:" + pd.getDiagramResourceName());
                System.out.println("部署对象ID：" + pd.getDeploymentId());
                System.out.println("#########################################################");
            }
        }

        // 注意 这里需要拿007来查询，key-value需要拿value来获取任务
        List<Task> list = taskService.createTaskQuery().taskAssignee("007").list();
        if (list != null && list.size() > 0) {
            for (org.activiti.engine.task.Task task : list) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务的创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("#######################################");
            }
        }

        // 设置User Task2的受理人变量
        Map<String, Object> variables1 = new HashMap<>();
        variables1.put("user2", "Kevin");
        // 因为007只有一个代办的任务，直接完成任务，并赋值下一个节点的受理人user2为Kevin办理
        taskService.complete(list.get(0).getId(), variables1);
        System.out.println("User Task1被完成了，此时流程已流转到User Task2");
    }

    @RequestMapping("multiAssignee")
    public void setMultiAssignee() {

        //根据bpmn文件部署流程
        repositoryService.createDeployment().addClasspathResource("MultiAssignee.bpmn").deploy();
        // 设置多个处理人变量 这里设置了三个人
        Map<String, Object> variables = new HashMap<>();
        List<String> userList = new ArrayList<>();
        userList.add("user1");
        userList.add("user2");
        userList.add("user3");
        variables.put("userList", userList);
        //采用key来启动流程定义并设置流程变量，返回流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("multiAssigneeProcess", variables);
        String processId = pi.getId();
        System.out.println("流程创建成功，当前流程实例ID：" + processId);

        // 查看user1的任务
        List<Task> list = taskService.createTaskQuery().taskAssignee("user1").list();
        if (list != null && list.size() > 0) {
            for (org.activiti.engine.task.Task task : list) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务的创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("#######################################");
            }
        }

        // 查看user2的任务
        List<Task> list2 = taskService.createTaskQuery().taskAssignee("user2").list();
        if (list2 != null && list2.size() > 0) {
            for (org.activiti.engine.task.Task task : list2) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务的创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("#######################################");
            }
        }

        // 查看user3的任务
        List<Task> list3 = taskService.createTaskQuery().taskAssignee("user3").list();
        if (list3 != null && list3.size() > 0) {
            for (org.activiti.engine.task.Task task : list3) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务的创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("#######################################");
            }
        }

    }

    @RequestMapping("exclusiveGateway")
    public void exclusiveGateway() {

        //根据bpmn文件部署流程
        repositoryService.createDeployment().addClasspathResource("exclusiveGateway.bpmn").deploy();
        // 设置User Task1受理人变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("user1", "007");
        //采用key来启动流程定义并设置流程变量，返回流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("exclusiveGatewayAndTimerBoundaryEventProcess", variables);
        String processId = pi.getId();
        System.out.println("流程创建成功，当前流程实例ID：" + processId);

        // 注意 这里需要拿007来查询，key-value需要拿value来获取任务
        List<Task> list = taskService.createTaskQuery().taskAssignee("007").list();
        Map<String, Object> variables1 = new HashMap<>();
        variables1.put("user2", "lili"); // 设置User Task2的受理人变量
        variables1.put("operate", ""); // 设置用户的操作 为空 表示走flow3的默认路线
        taskService.complete(list.get(0).getId(), variables1);
        System.out.println("User Task1被完成了，此时流程已流转到User Task2");

        List<Task> list1 = taskService.createTaskQuery().taskAssignee("lili").list();
        Map<String, Object> variables2 = new HashMap<>();
        variables2.put("user4", "bobo");
        variables2.put("startTime", "2019-3-4T10:15:00"); // 设置定时边界任务的触发时间 注意：后面的时间必须是ISO 8601时间格式的字符串！！！
        taskService.complete(list1.get(0).getId(), variables2);

        List<Task> list2 = taskService.createTaskQuery().taskAssignee("bobo").list();
        if (list2 != null && list2.size() > 0) {
            for (org.activiti.engine.task.Task task : list2) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务的创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("#######################################");
            }
        }
    }

}
