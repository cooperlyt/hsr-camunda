package cc.coopersoft.hsr.camunda.messages;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class CreateListener {


  @Bean
  public Consumer<Message<SimpleMsg>> processCreateChannel() {
    return msg -> {
      Object arg = msg.getHeaders();
      log.info(Thread.currentThread().getName() + " Receive New Messages: " + msg.getPayload().getMsg() + " ARG:"
          + arg.toString());
    };
  }

}
