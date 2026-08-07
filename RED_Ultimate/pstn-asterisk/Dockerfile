FROM andrius/asterisk:20

LABEL maintainer="RED Sovereign Engineering © 2026"
LABEL version="2.0.0-UNIFIED"
LABEL system="B - PSTN DINSTAR UC2000-VE-8T Unified"

COPY extensions.conf /etc/asterisk/extensions.conf
COPY docker-entrypoint.sh /usr/local/bin/red-asterisk-entrypoint
# Windows checkouts may present CRLF even for shell files; normalize inside the image before exec.
RUN sed -i 's/\r$//' /usr/local/bin/red-asterisk-entrypoint /etc/asterisk/extensions.conf \
    && chmod 0755 /usr/local/bin/red-asterisk-entrypoint \
    && rm -f /etc/asterisk/manager.conf /etc/asterisk/pjsip.conf \
    && mkdir -p /var/log/asterisk /var/spool/asterisk

EXPOSE 5060/udp 5060/tcp 5038 8088 10000-10100/udp

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=20s \
  CMD asterisk -rx 'core show version' || exit 1

ENTRYPOINT ["/usr/local/bin/red-asterisk-entrypoint"]
