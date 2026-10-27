# RED PSTN bridge (Asterisk + DINSTAR UC2000-VE-8T) — voice only.
# Build context: platform/telephony (configuration lives there; code lives nowhere).
FROM andrius/asterisk:20

LABEL org.opencontainers.image.title="RED PSTN Gateway"
LABEL org.opencontainers.image.description="Asterisk bridge to the DINSTAR GSM/VS gateway"

COPY extensions.conf /etc/asterisk/extensions.conf
COPY docker-entrypoint.sh /usr/local/bin/red-asterisk-entrypoint

# Windows checkouts may present CRLF even for shell files; normalize inside the image.
RUN sed -i 's/\r$//' /usr/local/bin/red-asterisk-entrypoint /etc/asterisk/extensions.conf \
    && chmod 0755 /usr/local/bin/red-asterisk-entrypoint \
    && rm -f /etc/asterisk/manager.conf /etc/asterisk/pjsip.conf \
    && mkdir -p /var/log/asterisk /var/spool/asterisk

EXPOSE 5060/udp 5060/tcp 10000-10100/udp

HEALTHCHECK --interval=30s --timeout=10s --retries=3 --start-period=30s \
  CMD asterisk -rx 'core show version' || exit 1

ENTRYPOINT ["/usr/local/bin/red-asterisk-entrypoint"]
