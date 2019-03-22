package pers.zc.activiti;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RunWith(SpringRunner.class)
@SpringBootTest
public class ApplicationTests {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessEngine processEngine;

    private String processId = "85005";

    @Test
    public void deploy() {
        //bpmn文件部署
        repositoryService.createDeployment().addClasspathResource("Test.bpmn").name("Test").deploy();
        //启动流程
        // 设置User Task1受理人变量
        Map<String, Object> variable = new HashMap<>();
        variable.put("user", "Jay.A");
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("test", variable);
        processId = processInstance.getId();
        String name = processInstance.getName();
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
        variables.put("user", "Jay.B");
        taskService.complete(taskList.get(0).getId(), variables);
        System.out.println("[TaskA]任务完成，任务传给：Jay.B");
    }

    @Test
    public void query() {
        System.out.println("[query]当前流程实例ID：" + processId);
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                .processInstanceId(processId)
                .list();
        //任务逻辑
        if (taskList.size() != 0) {
            System.out.println("任务查询成功，任务列表：" + taskList);
            for (Task task : taskList) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务执行人：" + task.getAssignee());
            }
        }else{
            System.out.println("流程不存在或已经执行完成");
        }
    }

    @Test
    public void taskB() {
        System.out.println("[taskB]流程创建成功，当前流程实例ID：" + processId);
        //任务逻辑
        List<Task> taskList = taskService.createTaskQuery()
                .processInstanceId(processId)
                .taskAssignee("Jay.B")
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }
        //任务完成，向下执行
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", "Jay.C");
        taskService.complete(taskList.get(0).getId(), variables);
        System.out.println("[taskB]任务完成，任务传给：Jay.C");
    }

    @Test
    //任务驳回
    public void taskC2A() {
        System.out.println("[taskC]流程创建成功，当前流程实例ID：" + processId);
        //任务逻辑
        List<Task> taskList = taskService.createTaskQuery()
                .processInstanceId(processId)
                .taskAssignee("Jay.C")
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }
        //任务完成，向下执行
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", "Jay.A");
        variables.put("status", -1);
        taskService.complete(taskList.get(0).getId(), variables);
        System.out.println("[taskC]任务驳回，任务传给：Jay.A");
    }

    @Test
    public void taskC2D() {
        System.out.println("[taskC2D]流程创建成功，当前流程实例ID：" + processId);
        //任务逻辑
        List<Task> taskList = taskService.createTaskQuery()
                .processInstanceId(processId)
                .taskAssignee("Jay.C")
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }
        //任务完成，向下执行
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", "Jay.D");
        variables.put("status", 1);
        taskService.complete(taskList.get(0).getId(), variables);
        System.out.println("[taskC2D]任务完成，任务传给：Jay.D");
    }


    @Test
    public void taskA() {
        System.out.println("[taskA]流程创建成功，当前流程实例ID：" + processId);
        //任务逻辑
        List<Task> taskList = taskService.createTaskQuery()
                .processInstanceId(processId)
                .taskAssignee("Jay.A")
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }
        //任务完成，向下执行
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", "Jay.B");
        taskService.complete(taskList.get(0).getId(), variables);
        System.out.println("[taskA]任务完成，任务传给：Jay.B");
    }

    @Test
    public void taskD() {
        System.out.println("[taskD]流程创建成功，当前流程实例ID：" + processId);
        //任务逻辑
        List<Task> taskList = taskService.createTaskQuery()
                .processInstanceId(processId)
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }
        //任务完成，向下执行
        taskService.complete(taskList.get(0).getId());
        System.out.println("[taskD]任务完成，流程完结");
    }


    public void queryHistory(){

    }

}
