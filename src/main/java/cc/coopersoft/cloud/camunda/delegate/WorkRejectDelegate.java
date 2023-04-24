package cc.coopersoft.cloud.camunda.delegate;

import cc.coopersoft.cloud.camunda.Constants;
import cc.coopersoft.cloud.camunda.messages.ProcessChangeService;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;

@Slf4j
public class WorkRejectDelegate implements JavaDelegate {

  private final ProcessChangeService processChangeService;

  public WorkRejectDelegate(ProcessChangeService processChangeService) {
    this.processChangeService = processChangeService;
  }

  @Override
  public void execute(DelegateExecution delegateExecution) throws Exception {
    log.debug("work reject define: {}", delegateExecution.getProcessBusinessKey());
    processChangeService.workStatusChange(delegateExecution,Constants.WorkStatus.REJECT);
  }
}
