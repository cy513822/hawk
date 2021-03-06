package com.oc.hawk.kubernetes.runtime.application.runtime.spec;

import com.google.common.collect.Maps;
import com.oc.hawk.kubernetes.runtime.application.entrypoint.ClusterIPType;
import io.fabric8.kubernetes.api.model.IntOrString;
import io.fabric8.kubernetes.api.model.ServicePort;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Getter
@Builder
public class NetworkConfigSpec {
    private static final Integer ENV_DEFAULT_SERVER_PORT = 8080;
    private final String PORT_NAME_HTTP = "http";
    /**
     * Just support TCP value , optional values ["SCTP", "TCP", "UDP"]
     */
    private final String HTTP_PROTOCOL = "TCP";
    private final String TCP_PROTOCOL = "TCP";

    private final Integer innerPort;
    private final Boolean mesh;
    private final String hosts;
    private final String serviceName;
    private Map<Integer, Integer> extraPorts;

    public ClusterIPType getNetworkType() {

        if (this.extraPorts == null || this.extraPorts.isEmpty()) {
            return ClusterIPType.ClusterIP;
        }
        return ClusterIPType.NodePort;
    }

    public ServicePort[] getServicePorts() {
        String serviceName = this.getServiceName();
        Map<Integer, Integer> nonNullExtraPorts = this.getNonNullExtraPorts();
        List<ServicePort> ports = nonNullExtraPorts.entrySet().stream().map(entry -> {
            IntOrString targetPort = new IntOrString(entry.getKey());
            Integer nodePort = entry.getValue();
            Integer port = entry.getKey();

            String portName = serviceName + "-" + port;
            return new ServicePort(TCP_PROTOCOL, portName, nodePort, port, TCP_PROTOCOL, targetPort);
        }).collect(Collectors.toList());

        IntOrString targetPort = new IntOrString(this.getInnerPort());

        // https://preliminary.istio.io/zh/docs/ops/deployment/requirements/
        if (!nonNullExtraPorts.containsKey(ENV_DEFAULT_SERVER_PORT)) {
            ports.add(new ServicePort(HTTP_PROTOCOL, serviceName + "-" + PORT_NAME_HTTP, 0, ENV_DEFAULT_SERVER_PORT, HTTP_PROTOCOL, targetPort));
        }
        return ports.toArray(new ServicePort[0]);
    }

    private Map<Integer, Integer> getNonNullExtraPorts() {
        if (extraPorts == null) {
            extraPorts = Maps.newHashMap();
        }
        return extraPorts;
    }
}
