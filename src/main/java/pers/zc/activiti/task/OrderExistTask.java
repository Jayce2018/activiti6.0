package pers.zc.activiti.task;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.JavaDelegate;

public class OrderExistTask implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("---------------------------------------------");
        System.out.println();
        System.out.println("OrderExistTask Service " + this.toString()
                + "Is Saying Hello To Every One !");
        System.out.println("---------------------------------------------");
        System.out.println();
        execution.setVariable("status","true");
    }
}
