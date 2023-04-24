package cc.coopersoft.cloud.camunda.messages;

import cc.coopersoft.cloud.camunda.Constants;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.springframework.cloud.stream.function.StreamBridge;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class ProcessChangeService {

  private final StreamBridge streamBridge;

  public ProcessChangeService(StreamBridge streamBridge) {
    this.streamBridge = streamBridge;
  }

  private void workStatusChange(String workId, String define, Constants.WorkStatus status) throws Exception {
    var msg = MessageBuilder.withPayload(status.name())
        .setHeader(Constants.WORK_KEY_NAME,workId)
        .setHeader(Constants.DEFINE_KEY_NAME,define)
        .build();
    if (!streamBridge.send("processChangeChannel-out-0",msg)){
      throw new Exception("message send fail!");
    }

  }

  public void workStatusChange(DelegateExecution delegateExecution,Constants.WorkStatus status) throws Exception {
    String define = (String) delegateExecution.getVariable(Constants.DEFINE_KEY_NAME);
    String workId = delegateExecution.getBusinessKey();
    delegateExecution.setVariable(Constants.STATUS_KEY_NAME,status.name());
    log.debug("work reject define: {}, id: {}", define, workId);
    workStatusChange(workId,define,status);
  }
}
