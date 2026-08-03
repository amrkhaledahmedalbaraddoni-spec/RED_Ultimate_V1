FROM andrius/asterisk:20

LABEL maintainer="RED Sovereign © 2026"
LABEL version="2.0.0-ULTIMATE"
LABEL system="B - PSTN DINSTAR UC2000-VE-8T"

# Copy custom configurations - Ultimate
COPY pjsip.conf /etc/asterisk/pjsip.conf
COPY pjsip_dinstar.conf /etc/asterisk/pjsip_dinstar.conf
COPY extensions.conf /etc/asterisk/extensions.conf
COPY manager.conf /etc/asterisk/manager.conf

# Create dirs
RUN mkdir -p /var/log/asterisk /var/spool/asterisk && \
    chown -R asterisk:asterisk /etc/asterisk /var/log/asterisk /var/spool/asterisk 2>/dev/null || true

EXPOSE 5060/udp 5060/tcp 5038 8088 10000-10100/udp

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=20s \
  CMD asterisk -rx 'core show version' || exit 1

CMD ["/usr/sbin/asterisk", "-f", "-v"]
