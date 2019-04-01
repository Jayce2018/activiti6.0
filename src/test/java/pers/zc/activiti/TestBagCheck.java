package pers.zc.activiti;

import com.alibaba.fastjson.JSONArray;
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
public class TestBagCheck {
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;
    @Autowired
    private ProcessEngine processEngine;

    private String processId = "142505";

    @Test
    public void deploy() {
        //bpmn文件部署
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("TestBagSampleCheck.bpmn").name("TestBagCheck").deploy();
        //启动流程
        // 设置User Task1受理人变量
        Map<String, Object> variable = new HashMap<>();
        String [] candidateUsers={"Jay.B","Jay.B2","Jay.B3","Jay.B4"};
        List<String> stringList = Arrays.asList(candidateUsers);
        variable.put("users", stringList);
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("test", "businessKey", variable);
        processId = processInstance.getId();

        String name = processInstance.getName();
        System.out.println("[deploy]流程创建成功，当前流程实例ID：" + processId);
        System.out.println("流程创建成功，当前流程实例NAME：" + name);
        //任务逻辑
        List<Task> taskList = processEngine.getTaskService().createTaskQuery().processInstanceId(processId).list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }
        //任务完成，向下执行

        // 由于流程用户上下文对象是线程独立的，所以要在需要的位置设置，要保证设置和获取操作在同一个线程中
        Authentication.setAuthenticatedUserId("Jay.B");//批注人的名称  一定要写，不然查看的时候不知道人物信息
        // 添加批注信息
        taskService.addComment(taskList.get(0).getId(), null, "A完成批注信息");//comment为批注内容
        Task task = taskList.get(0);
        taskService.complete(taskList.get(0).getId());
        System.out.println("[TaskA]任务完成，任务传给：Jay.B");
    }

    //当人任务查询
    @Test
    public void query() {
        System.out.println("[query]当前流程实例ID：" + processId);
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                .taskCandidateUser("Jay.B2")
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
                .processInstanceBusinessKey("businessKey")
                //.processInstanceId(processId)
                .singleResult();
        //2）通过流程实例查询所有的(用户任务类型)历史活动
        List<HistoricActivityInstance> hais = processEngine.getHistoryService().createHistoricActivityInstanceQuery()
                //.processInstanceId(pi.getId())
                .processDefinitionId("test")
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

    @Test
    public void taskB() {
        System.out.println("[taskB]流程创建成功，当前流程实例ID：" + processId);
        //任务逻辑
        List<Task> taskList = processEngine.getTaskService().createTaskQuery()
                .taskCandidateUser("Jay.W")
                .processInstanceId(processId)
                //.taskAssignee("Jay.B")
                .list();
        System.out.println("任务查询成功，任务列表：" + taskList);
        for (Task task : taskList) {
            System.out.println("任务ID：" + task.getId());
            System.out.println("任务执行人：" + task.getAssignee());
        }
        //任务完成，向下执行
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", "Jay.C");
        taskList.get(0).setAssignee("hhhha");
        taskService.complete(taskList.get(0).getId(), variables);
        System.out.println("[taskB]任务完成，任务传给：Jay.C");
    }

    public void taskQuery(){
        TaskQuery taskQuery = taskService.createTaskQuery().taskId("102510");
        System.out.println(taskQuery);
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


    @Test
    public void queryHistory() {
        /*List<TaskBo> taskBoList = new ArrayList<TaskBo>();
        HistoricProcessInstance hisProcessInstance = (HistoricProcessInstance) historyService
                .createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey(businessEventId).singleResult();
        // 该流程实例的所有节点审批记录
        List<HistoricActivityInstanceQuery> hisActInstList = getHisUserTaskActivityInstanceList(hisProcessInstance
                .getId());
        for (Iterator iterator = hisActInstList.iterator(); iterator.hasNext();) {
            // 需要转换成HistoricActivityInstance
            HistoricActivityInstance activityInstance = (HistoricActivityInstance) iterator
                    .next();
            //如果还没结束则不放里面
            if ("".equals(taskBo.getEndTime()) || taskBo.getEndTime() == null) {
                continue;
            }
            TaskBo taskBo = new TaskBo();
            taskBo.setTaskName(activityInstance.getActivityName());
            // 获得审批人名称 Assignee存放的是审批用户id
            if (activityInstance.getAssignee() != null) {
                taskBo.setApproveUserName(getUserName(activityInstance
                        .getAssignee()));
            } else {
                taskBo.setApproveUserName("");
            }
            // 获取流程节点开始时间
            taskBo.setStartTime(activityInstance.getStartTime() != null ? DateTimeUtil
                    .getFormatDate(activityInstance.getStartTime(),
                            WorkflowConstants.DATEFORMATSTRING) : "");
            // 获取流程节点结束时间
            if (activityInstance.getEndTime() == null) {
                taskBo.setEndTime("");
            } else {
                taskBo.setEndTime(DateTimeUtil.getFormatDate(
                        activityInstance.getEndTime(),
                        WorkflowConstants.DATEFORMATSTRING));
            }
            taskBoList.add(taskBo);
        }
        return taskBoList;*/
    }

}
