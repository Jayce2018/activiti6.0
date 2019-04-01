package pers.zc.activiti.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Service;

@Service
public class StatusUpdateTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("---------------------------------------------");
        System.out.println();
        System.out.println("StatusUpdateTask Service " + this.toString()
                + "Is Saying Hello To Every One !");
        System.out.println("---------------------------------------------");
        System.out.println();
        execution.setVariable("status","true");
    }
}
