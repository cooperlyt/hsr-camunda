package cc.coopersoft.cloud.camunda.messages;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.impl.history.event.HistoryEvent;
import org.camunda.bpm.spring.boot.starter.event.ExecutionEvent;
import org.camunda.bpm.spring.boot.starter.event.TaskEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CamundaListener {

  @EventListener
  public void onTaskEvent(DelegateTask taskDelegate) {
    log.debug("mutable task event: {}", taskDelegate.getEventName());
    // handle mutable task event
  }

  @EventListener
  public void onTaskEvent(TaskEvent taskEvent) {
    log.debug("immutable task event: {}", taskEvent.getEventName());
    // handle immutable task event
  }

  @EventListener
  public void onExecutionEvent(DelegateExecution executionDelegate) {
    log.debug("immutable execution event: {}", executionDelegate.getEventName());
    // handle mutable execution event
  }

  @EventListener
  public void onExecutionEvent(ExecutionEvent executionEvent) {
    log.debug("immutable execution event: {}", executionEvent.getEventName());
    // handle immutable execution event
  }

  @EventListener
  public void onHistoryEvent(HistoryEvent historyEvent) {
    log.debug("history event");
    // handle history event
  }

}
