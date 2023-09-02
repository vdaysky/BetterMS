package obfuscate.util.alg;

import java.util.List;

public class WithinBlockTrace {

    private boolean isInBlock;


    private List<TraceEventV2> traces;


    public WithinBlockTrace(boolean isInBlock, List<TraceEventV2> traces) {
        this.isInBlock = isInBlock;
        this.traces = traces;
    }

    public boolean isInBlock() {
        return isInBlock;
    }

    public List<TraceEventV2> getTraces() {
        return traces;
    }
}
