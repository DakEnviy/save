package ua.deamonish.modulesystem.module.event;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.events.PacketAdapter;
import ua.deamonish.modulesystem.module.Module;


/**
 * Регистрация и удаление эвентов ProtocolLib
 */
public class ModuleDataProtocolLibListener implements ModuleData {

	@Override
	public void register(Module module, Object o) {
		PacketAdapter adapter = (PacketAdapter) o;
		ProtocolLibrary.getProtocolManager().addPacketListener(adapter);
	}

	@Override
	public void unregister(Module module, Object o) {
		PacketAdapter adapter = (PacketAdapter) o;
		ProtocolLibrary.getProtocolManager().removePacketListener(adapter);
	}
}
