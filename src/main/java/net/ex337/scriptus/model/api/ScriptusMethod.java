package net.ex337.scriptus.model.api;

public class ScriptusMethod {
    private String methodName;
    private Class<?>[] methodArgs;
    private String quickSyntax;
    private String quickDesc;
    private boolean separateImpl;

    public ScriptusMethod(String methodName, String quickSyntax, String quickDesc, boolean hasSeparateImpl) {
        this.methodName = methodName;
        this.quickSyntax = quickSyntax;
        this.quickDesc = quickDesc;
        this.separateImpl = hasSeparateImpl;
    }

    public ScriptusMethod(String methodName, String quickSyntax, String quickDesc, Class<?>... methodArgs) {
        this.methodName = methodName;
        this.methodArgs = methodArgs;
        this.quickSyntax = quickSyntax;
        this.quickDesc = quickDesc;
    }

    public String getMethodName() {
        return methodName;
    }

    public Class<?>[] getMethodArgs() {
        return methodArgs;
    }

    public String getQuickSyntax() {
        return quickSyntax;
    }

    public String getQuickDesc() {
        return quickDesc;
    }

    public boolean hasSeparateImpl() {
        return separateImpl;
    }
    
    
}