package cc.coopersoft.cloud.camunda.messages;

import cc.coopersoft.cloud.camunda.Constants;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Slf4j
@Component("processStatusChangeTransactionListener")
public class ProcessStatusChangeTransactionListener implements TransactionListener {


  private final RuntimeService runtimeService;

  public ProcessStatusChangeTransactionListener(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

  @Override
  public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
    return LocalTransactionState.COMMIT_MESSAGE;
  }

  @Override
  public LocalTransactionState checkLocalTransaction(MessageExt msg) {
    String status = msg.getProperty(Constants.STATUS_KEY_NAME);
    String workId = msg.getProperty(Constants.WORK_KEY_NAME);

    try {
      if (StringUtils.hasText(status) && StringUtils.hasText(workId) &&
          runtimeService.createProcessInstanceQuery()
              .processInstanceBusinessKey(workId)
              .variableValueEquals(Constants.STATUS_KEY_NAME, status)
              .count() >= 1) {
        return LocalTransactionState.COMMIT_MESSAGE;
      }
    }catch (Exception e){
      log.error("status status change fail",e);
    }
    return LocalTransactionState.UNKNOW;
  }
}
