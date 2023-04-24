package cc.coopersoft.cloud.camunda.messages;

import cc.coopersoft.cloud.camunda.Constants;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;

import java.util.Collections;
import java.util.function.Consumer;

@Slf4j
@Configuration
public class CreateListener {

  private final RuntimeService runtimeService;

  public CreateListener(RuntimeService runtimeService) {
    this.runtimeService = runtimeService;
  }

//  process_project_license

  @Bean
  public Consumer<Message<String>> processCreateChannel() {
    return msg -> {
       var arg = msg.getHeaders();
       String workId = String.valueOf(arg.get(Constants.WORK_KEY_NAME, Long.class));
       String define = arg.get(Constants.DEFINE_KEY_NAME,String.class);
//      approval
      runtimeService.startProcessInstanceById(msg.getPayload(),workId,workId,
          Collections.singletonMap(Constants.DEFINE_KEY_NAME,define));

      log.info(Thread.currentThread().getName() + " Receive New Messages: " + msg.getPayload()+ " ARG:"
          + arg);
    };
  }

}
