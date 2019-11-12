package com.deemo.attach;

import com.sun.tools.attach.VirtualMachine;
import com.sun.tools.attach.VirtualMachineDescriptor;

import java.util.List;

public class AttachThread extends Thread {

    private final String jarPath;
    private final List<VirtualMachineDescriptor> beforeVM;

    public AttachThread(String jarPath, List<VirtualMachineDescriptor> beforeVM) {
        this.jarPath = jarPath;
        this.beforeVM = beforeVM;
    }

    @Override
    public void run() {
        int i = 0;

        do {
            try {
                VirtualMachine vm = null;
                List<VirtualMachineDescriptor> listAfter = VirtualMachine.list();
                for (VirtualMachineDescriptor vmd : listAfter) {
                    if (!beforeVM.contains(vmd)) {
                        // 如果 VM 有增加，我们就认为是被监控的 VM 启动了
                        // 这时，我们开始监控这个 VM
                        System.out.println("正在入侵...qqqqqq");
                        vm = VirtualMachine.attach(vmd);
                        break;
                    }
                }

                if (null != vm) {
                    vm.loadAgent(jarPath);
                    vm.detach();
                    break;
                }

                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println("Attach error." + e);
                break;
            }
        } while (++i < 20);
    }

    public static void main(String[] args) {
        AttachThread attachThread = new AttachThread("E:\\WorkSpace\\JustTryHard\\JustTryHard-Agent\\Agent\\target\\Agent-1.0-SNAPSHOT.jar", VirtualMachine.list());
        attachThread.start();
    }

}
