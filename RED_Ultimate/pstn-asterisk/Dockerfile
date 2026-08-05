FROM andrius/asterisk

COPY extensions.conf /etc/asterisk/extensions.conf
COPY docker-entrypoint.sh /usr/local/bin/red-asterisk-entrypoint
RUN chmod 0755 /usr/local/bin/red-asterisk-entrypoint \
    && rm -f /etc/asterisk/manager.conf /etc/asterisk/pjsip.conf

EXPOSE 5060/udp 5038 8088 10000-10100/udp
ENTRYPOINT ["/usr/local/bin/red-asterisk-entrypoint"]
