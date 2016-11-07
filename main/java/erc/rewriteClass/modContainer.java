package erc.rewriteClass;

import java.util.Arrays;

import cpw.mods.fml.common.DummyModContainer;
import cpw.mods.fml.common.LoadController;
import cpw.mods.fml.common.ModMetadata;

public class modContainer extends DummyModContainer {
	
	public modContainer() {
		super(new ModMetadata());
	 
		// @Mod�̂悤�ɋL�q���܂�(mcmod.info�͎g���Ȃ�)�B
		ModMetadata meta = super.getMetadata();
		meta.modId = "ercclasstransform";
		meta.name = "ERCClassTransform";
		meta.version = "1.0";
		// �ȉ��͏ȗ���
		meta.authorList = Arrays.asList(new String[] { "MOTTY" });
		this.setEnabledState(true);
	}
	
	@Override
	public boolean registerBus(com.google.common.eventbus.EventBus bus, LoadController lc) {
		bus.register(this);
		return true;
	}

}
