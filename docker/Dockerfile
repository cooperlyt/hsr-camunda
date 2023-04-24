FROM ibm-semeru-runtimes:open-17-jre
ENV TZ=Asia/Shanghai \
    DEBIAN_FRONTEND=noninteractive
RUN apt-get -y update && apt-get -y upgrade \
    && apt-get -y install --no-install-recommends \
    tzdata \
    && ln -fs /usr/share/zoneinfo/${TZ} /etc/localtime \
    && echo ${TZ} > /etc/timezone \
    && dpkg-reconfigure --frontend noninteractive tzdata \
    && rm -rf /var/lib/apt/lists/*  \
    && mkdir -p /usr/local/app \
    && mkdir -p /opt/config
ADD @project.build.finalName@.jar /usr/local/app/
ADD ca.pem /usr/local/app/
RUN echo yes | keytool -importcert -trustcacerts -cacerts -file /usr/local/app/ca.pem -alias feipuCert -storepass changeit
ADD wait-for-it.sh wait-for-it.sh
RUN chmod +x wait-for-it.sh
ADD run.sh run.sh
RUN chmod +x run.sh
CMD ./run.sh

