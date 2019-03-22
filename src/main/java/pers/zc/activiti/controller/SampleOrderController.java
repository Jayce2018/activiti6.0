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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zc 2018-06-04
 */
@RestController
@RequestMapping("/sampleOrder")
public class SampleOrderController {

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private RuntimeService runtimeService;
    @Autowired
    private TaskService taskService;

    @Autowired
    private ProcessEngine processEngine;

    //go
    private String processId = "57505";

    //TODO dong 发起创建样单，指定西负责填报价单
    @RequestMapping("createSample")
    public void createSample() {
        //根据bpmn文件部署流程
        repositoryService.createDeployment().addClasspathResource("SampleOrder.bpmn").name("SampleOrder").deploy();
        // 设置User Task1受理人变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("user", "dong");
        //采用key来启动流程定义并设置流程变量，返回流程实例
        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("sampleOrder", variables);
        String processId = processInstance.getId();
        System.out.println("流程创建成功，当前流程实例ID：" + processId);
        //获取当前登录人任务
        List<Task> list = taskService.createTaskQuery().taskAssignee("dong").processInstanceId(processId).list();
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务步骤：创建样单");
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
        variables1.put("user", "xi");
        // 因为007只有一个代办的任务，直接完成任务，并赋值下一个节点的受理人user2为Kevin办理
        taskService.complete(list.get(0).getId(), variables1);
        System.out.println("样单创建被【东】完成了，此时流程已流转到【西】创建报价单");
    }

    @RequestMapping("exctue/xi")
    public void exctueXi() throws Exception {
        System.out.println("[exctue/xi]"+processId);
        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                /** 查询条件（where部分） */
                .taskAssignee("xi")// 指定个人任务查询，指定办理人
                // .taskCandidateUser(candidateUser)//组任务的办理人查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                .processInstanceId(processId)//使用流程实例ID查询
                // .executionId(executionId)//使用执行对象ID查询
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                // .singleResult()//返回惟一结果集
                // .count()//返回结果集的数量
                // .listPage(firstResult, maxResults);//分页查询
                .list();// 返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
            // 设置User Task2的受理人变量
            Map<String, Object> variables1 = new HashMap<>();
            variables1.put("user", "nan");
            // 因为007只有一个代办的任务，直接完成任务，并赋值下一个节点的受理人user2为Kevin办理
            taskService.complete(list.get(0).getId(), variables1);
            System.out.println("报价单创建被【西】完成了，此时流程已流转到【南】打样申请");
        } else {
            throw new Exception("当前西不存在待执行的任务！");
        }
    }

    @RequestMapping("exctue/nan")
    public void exctueNan() throws Exception {
        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                /** 查询条件（where部分） */
                .taskAssignee("nan")// 指定个人任务查询，指定办理人
                // .taskCandidateUser(candidateUser)//组任务的办理人查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                .processInstanceId(processId)//使用流程实例ID查询
                // .executionId(executionId)//使用执行对象ID查询
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                // .singleResult()//返回惟一结果集
                // .count()//返回结果集的数量
                // .listPage(firstResult, maxResults);//分页查询
                .list();// 返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
            // 设置User Task2的受理人变量
            Map<String, Object> variables1 = new HashMap<>();
            variables1.put("user", "bei");
            // 因为007只有一个代办的任务，直接完成任务，并赋值下一个节点的受理人user2为Kevin办理
            taskService.complete(list.get(0).getId(), variables1);
            System.out.println("打样申请创建被【南】完成了，此时流程已流转到【北】成本会计审核");
        } else {
            throw new Exception("当前南不存在待执行的任务！");
        }
    }

    @RequestMapping("exctue/bei/agree")
    public void exctueBeiAgree() throws Exception {
        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                /** 查询条件（where部分） */
                .taskAssignee("bei")// 指定个人任务查询，指定办理人
                // .taskCandidateUser(candidateUser)//组任务的办理人查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                .processInstanceId(processId)//使用流程实例ID查询
                // .executionId(executionId)//使用执行对象ID查询
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                // .singleResult()//返回惟一结果集
                // .count()//返回结果集的数量
                // .listPage(firstResult, maxResults);//分页查询
                .list();// 返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
            // 设置User Task2的受理人变量
            Map<String, Object> variables1 = new HashMap<>();
            variables1.put("user", "zhong");
            variables1.put("result", "agree");
            // 因为007只有一个代办的任务，直接完成任务，并赋值下一个节点的受理人user2为Kevin办理
            taskService.complete(list.get(0).getId(), variables1);
            System.out.println("打样申请被【北】审核通过，此时流程已流转到【中】板房加工");
        } else {
            throw new Exception("当前北不存在待执行的任务！");
        }
    }

    @RequestMapping("exctue/zhong")
    public void exctueZhong() throws Exception {
        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                /** 查询条件（where部分） */
                .taskAssignee("zhong")// 指定个人任务查询，指定办理人
                // .taskCandidateUser(candidateUser)//组任务的办理人查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                .processInstanceId(processId)//使用流程实例ID查询
                // .executionId(executionId)//使用执行对象ID查询
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                // .singleResult()//返回惟一结果集
                // .count()//返回结果集的数量
                // .listPage(firstResult, maxResults);//分页查询
                .list();// 返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
            // 设置User Task2的受理人变量
            Map<String, Object> variables1 = new HashMap<>();
            variables1.put("user", "dongdong");
            // 因为007只有一个代办的任务，直接完成任务，并赋值下一个节点的受理人user2为Kevin办理
            taskService.complete(list.get(0).getId(), variables1);
            System.out.println("板房加工创建被【中】完成了，此时流程已流转到【东东】打样完成");
        } else {
            throw new Exception("当前中不存在待执行的任务！");
        }
    }

    @RequestMapping("exctue/dongdong")
    public void exctueDongdong() throws Exception {
        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                /** 查询条件（where部分） */
                .taskAssignee("dongdong")// 指定个人任务查询，指定办理人
                // .taskCandidateUser(candidateUser)//组任务的办理人查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                .processInstanceId(processId)//使用流程实例ID查询
                // .executionId(executionId)//使用执行对象ID查询
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                // .singleResult()//返回惟一结果集
                // .count()//返回结果集的数量
                // .listPage(firstResult, maxResults);//分页查询
                .list();// 返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
            // 设置User Task2的受理人变量
            Map<String, Object> variables1 = new HashMap<>();
            variables1.put("user", "dongxi");
            // 因为007只有一个代办的任务，直接完成任务，并赋值下一个节点的受理人user2为Kevin办理
            taskService.complete(list.get(0).getId(), variables1);
            System.out.println("打样完成创建被【东东】完成了，此时流程已流转到【东西】样品寄送");
        } else {
            throw new Exception("当前东东不存在待执行的任务！");
        }
    }

    @RequestMapping("exctue/dongxi")
    public void exctueDongxi() throws Exception {
        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                /** 查询条件（where部分） */
                .taskAssignee("dongxi")// 指定个人任务查询，指定办理人
                // .taskCandidateUser(candidateUser)//组任务的办理人查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                .processInstanceId(processId)//使用流程实例ID查询
                // .executionId(executionId)//使用执行对象ID查询
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                // .singleResult()//返回惟一结果集
                // .count()//返回结果集的数量
                // .listPage(firstResult, maxResults);//分页查询
                .list();// 返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
            // 设置User Task2的受理人变量
            taskService.complete(list.get(0).getId());
            System.out.println("打样完成创建被【东西】完成了，此时流程已完成");
        } else {
            throw new Exception("当前东西不存在待执行的任务！");
        }
    }

    @RequestMapping("exctue/bei/unagree")
    public void exctueBeiUnagree() throws Exception {
        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                /** 查询条件（where部分） */
                .taskAssignee("bei")// 指定个人任务查询，指定办理人
                // .taskCandidateUser(candidateUser)//组任务的办理人查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                .processInstanceId(processId)//使用流程实例ID查询
                // .executionId(executionId)//使用执行对象ID查询
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                // .singleResult()//返回惟一结果集
                // .count()//返回结果集的数量
                // .listPage(firstResult, maxResults);//分页查询
                .list();// 返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
            // 设置User Task2的受理人变量
            Map<String, Object> variables1 = new HashMap<>();
            variables1.put("result", "unagree");
            // 因为007只有一个代办的任务，直接完成任务，并赋值下一个节点的受理人user2为Kevin办理
            taskService.complete(list.get(0).getId(), variables1);
            System.out.println("打样申请被【北】审核不通过，此时流程已结束");
        } else {
            throw new Exception("当前北不存在待执行的任务！");
        }
    }

    @RequestMapping("singleAssignee")
    public void setSingleAssignee() {

        //根据bpmn文件部署流程
        Deployment deployment = repositoryService.createDeployment().addClasspathResource("SampleOrder.bpmn").name("SampleOrder").deploy();
        // 设置User Task1受理人变量
        Map<String, Object> variables = new HashMap<>();
        variables.put("user1", "dong");
        //采用key来启动流程定义并设置流程变量，返回流程实例
        ProcessInstance pi = runtimeService.startProcessInstanceByKey("sampleOrder", variables);
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
        List<Task> list = taskService.createTaskQuery().taskAssignee("dong").list();
        if (list != null && list.size() > 0) {
            for (Task task : list) {
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
        variables1.put("user2", "xi");
        // 因为007只有一个代办的任务，直接完成任务，并赋值下一个节点的受理人user2为Kevin办理
        taskService.complete(list.get(0).getId(), variables1);
        System.out.println("User Task1被完成了，此时流程已流转到User Task2");
    }

    @RequestMapping("task")
    public void task() {

        List<Task> list = processEngine.getTaskService()// 与正在执行的任务管理相关的Service
                .createTaskQuery()// 创建任务查询对象
                /** 查询条件（where部分） */
                //.taskAssignee(assignee)// 指定个人任务查询，指定办理人
                // .taskCandidateUser(candidateUser)//组任务的办理人查询
                // .processDefinitionId(processDefinitionId)//使用流程定义ID查询
                .processInstanceId("27525")//使用流程实例ID查询
                // .executionId(executionId)//使用执行对象ID查询
                /** 排序 */
                .orderByTaskCreateTime().asc()// 使用创建时间的升序排列
                /** 返回结果集 */
                // .singleResult()//返回惟一结果集
                // .count()//返回结果集的数量
                // .listPage(firstResult, maxResults);//分页查询
                .list();// 返回列表
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务ID:" + task.getId());
                System.out.println("任务名称:" + task.getName());
                System.out.println("任务的创建时间:" + task.getCreateTime());
                System.out.println("任务的办理人:" + task.getAssignee());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("执行对象ID:" + task.getExecutionId());
                System.out.println("流程定义ID:" + task.getProcessDefinitionId());
                System.out.println("########################################################");
            }
        }
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
            for (Task task : list) {
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
            for (Task task : list2) {
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
            for (Task task : list3) {
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
            for (Task task : list2) {
                System.out.println("任务ID：" + task.getId());
                System.out.println("任务的办理人：" + task.getAssignee());
                System.out.println("任务名称：" + task.getName());
                System.out.println("任务的创建时间：" + task.getCreateTime());
                System.out.println("流程实例ID：" + task.getProcessInstanceId());
                System.out.println("#######################################");
            }
        }
    }

    //TODO  获取当前登录人任务
    @RequestMapping(value = "currentUserInfo",method = {RequestMethod.GET,RequestMethod.POST})
    public void currentUserInfo(@RequestParam(value = "name") String name){
        System.out.println("获取当前登录人任务=>"+name);
        List<Task> list = taskService.createTaskQuery().taskAssignee(name).processInstanceId(processId).list();
        if (list != null && list.size() > 0) {
            for (Task task : list) {
                System.out.println("任务步骤：创建样单");
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
