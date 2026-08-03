FROM andrius/asterisk

# Copy custom configurations
COPY pjsip.conf /etc/asterisk/pjsip.conf
COPY extensions.conf /etc/asterisk/extensions.conf
COPY manager.conf /etc/asterisk/manager.conf

EXPOSE 5060/udp 5060/tcp 5038 8088

CMD ["/usr/sbin/asterisk", "-f"]
