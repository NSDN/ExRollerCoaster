package erc.entity;

import erc._core.ERC_Core;
import erc.message.ERC_MessageCoasterCtS;
import erc.message.ERC_PacketHandler;
import erc.tileEntity.TileEntityRailBase;
import erc.tileEntity.TileEntityRailBranch2;
import erc.tileEntity.Wrap_TileEntityRail;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.item.Item;
import net.minecraft.world.World;

/*
 * �P���̃N���C�A���g���ʒu�����R�[�X�^�[
 */
public class ERC_EntityCoasterMonodentate extends ERC_EntityCoaster{

	public ERC_EntityCoasterMonodentate(World world)
	{
		super(world);
	}
	
	public ERC_EntityCoasterMonodentate(World world, TileEntityRailBase tile, double x, double y, double z) {
		super(world, tile, x, y, z);
	}
	
	protected boolean canConnectForrowingCoaster()
	{
		return false;
	}
	
	public Item getItem()
    {
    	return ERC_Core.ItemCoasterMono;
    }

	@Override
	public void setParamFromPacket(float t, double speed, int x, int y, int z)
    {
    	 // ����Ă���̂�������������p�P�b�g����Ԃ��A���l�̂�N������ĂȂ��R�[�X�^�[�Ȃ�T�[�o�[�Ɠ���
    	if(this.riddenByEntity instanceof EntityClientPlayerMP)
    	{
    		if(tlrail==null)
    		{
    			if(checkTileEntity())
				{
    				killCoaster();
					return;
				}
    		}
	    	// send packet to server
	    	ERC_MessageCoasterCtS packet = new ERC_MessageCoasterCtS(getEntityId(), this.paramT, this.Speed, tlrail.xCoord, tlrail.yCoord, tlrail.zCoord);
		    ERC_PacketHandler.INSTANCE.sendToServer(packet);
    	}
    	else
    	{
    		Wrap_TileEntityRail rail = (Wrap_TileEntityRail)worldObj.getTileEntity(x,y,z);
    		if(rail == null)return;
    		if(rail instanceof TileEntityRailBranch2)return; // ���򃌁[����̂Ƃ��͓�����������Ƃ�߂Ăق���
    		
    		this.setParamT(t);
    		this.Speed = speed;
    		this.setRail( rail.getRail() );
//    		if(tlrail==null)
//    		{
//    			if(checkTileEntity())
//				{
//    				killCoaster();
//					return;
//				}
//    		}
    	}
		
    }
}
