package cc.coopersoft.hsr.camunda.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
public class WorkValidDelegate implements JavaDelegate {
  @Override
  public void execute(DelegateExecution delegateExecution) throws Exception {
    log.debug("call work valid");
  }
}
