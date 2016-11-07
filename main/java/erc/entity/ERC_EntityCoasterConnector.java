package erc.entity;

import java.util.UUID;

import erc._core.ERC_Core;
import erc.item.ERC_ItemCoasterConnector;
import erc.manager.ERC_CoasterAndRailManager;
import erc.manager.ERC_ManagerCoasterLoad;
import erc.message.ERC_MessageCoasterMisc;
import erc.message.ERC_MessageCoasterStC;
import erc.message.ERC_MessageRequestConnectCtS;
import erc.message.ERC_PacketHandler;
import erc.tileEntity.TileEntityRailBase;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class ERC_EntityCoasterConnector extends ERC_EntityCoaster {

	// private int isnotConnectParentFlag = -1;
	private float distanceToParent = -1;
	public int connectIndex;
	UUID parentuuid;
	ERC_EntityCoaster parent = null;

	// ERC_EntityCoasterConnector next = null;

	public ERC_EntityCoasterConnector(World world)
	{
		super(world);
		CoasterType = 1;
		if (worldObj.isRemote)
		{
			setModel(ModelID, CoasterType);
			setModelOptions();
		}
		parent = ERC_CoasterAndRailManager.client_getParentCoaster(world);
		if (parent != null)
			parent.connectionCoaster(this);
	}

	public ERC_EntityCoasterConnector(World world, TileEntityRailBase rail, double x, double y, double z) 
	{
		super(world, rail, x, y, z);
	}

	@Override
	public Item getItem() {
		return ERC_Core.ItemCoasterConnector;
	}

	// public void InitConnectorCoaster(int
	// isnotConnectParentFlag_ParentEntityID)
	// {
	// isnotConnectParentFlag = isnotConnectParentFlag_ParentEntityID;
	// }

	public double getSpeed() {
		return Speed;
	}

	public void setSumSpeed(double speed) {
		sumSpeed = speed;
	}

	// public void setConnectParentFlag(int num){isnotConnectParentFlag = num;}
	public void setParent(ERC_EntityCoaster parent) {
		this.parent = parent;
	}
	
	// return true to prevent any further processing.
    protected boolean requestConnectCoaster(EntityPlayer player)
    {
    	// �A���R�[�X�^�[���v���C���[�������ĂȂ������炾��
    	if(player.getCurrentEquippedItem() == null ) return false;
		if( !(player.getCurrentEquippedItem().getItem() instanceof ERC_ItemCoasterConnector) ) return false;
		
		//�N���b�N�����R�[�X�^�[���A��������Ă�̂���Ȃ��Ƃ��߁@�������e�R�[�X�^�[���A�������Ă���΂������ł��
		if( !canConnectForrowingCoaster() )
		{
			//�e�������Ȃ�悵
			if(parent.canConnectForrowingCoaster())
			{
				parent.AnswerRequestConnect(player);
				return true;
			}
			return false;
		}
		
		// �ȉ������A���̉E�N���b�N����͂����Ȃ�
		AnswerRequestConnect(player);
		return true;
    }
	
	protected boolean canConnectForrowingCoaster() 
	{
		return false;
	}

	public void initConnectParamT(float t, float distance)
	{
		if (parent != null && parent.tlrail != null)
			paramT = t - distance / parent.tlrail.Length;
		if (distanceToParent < 0)
			distanceToParent = distance;
		tlrail = parent.tlrail;
		AdjustParamT();
	}

	@Override
	public void killCoaster(boolean dropflag)
	{
		// if(worldObj.isRemote) return; //�N���͏����Kill���Ȃ�
		super.killCoaster(dropflag);
		// if(next!=null)next.killCoaster(dropflag);
		if (parent != null)
			parent.killPrevCoasters(dropflag, connectIndex);
		if (parent != null)
			parent.deleteConnectCoaster(this);
	}

	@Override
	public void onUpdate()
	{
		syncToClient();
		if (updateInit())
			return;

		savePrevData();

		updateParamT(); // �}��ϐ��̍X�V
		// �����[���𒴂����玟�̐ݒ�
		if (AdjustParamT())
			return;

		// AdjustParamT();

		// �V�[�g�̍��W�ݒ�
		// if(seats!=null)for(int i=0; i<seats.length; ++i)seats[i].onUpdate();

		// if(seats!=null)for(int i=0; i<seats.length;
		// ++i)if(seats[i]!=null)seats[i].updateRiderPosition2();
		updateSpeedAndRot();
		
		
		// �S
        if(seats!=null)
		{
    		for(int i=0; i<seats.length; ++i)if(seats[i]!=null)
    		{
    			if(!seats[i].addedToChunk && !worldObj.isRemote)worldObj.spawnEntityInWorld(seats[i]);
    			if(seats[i].updateFlag != this.updateFlag)seats[i]._onUpdate();
    		}
//    		if(worldObj.isRemote)ERC_Logger.debugInfo("end coaster onUpdate");
		}
        
        // �T
    	updateFlag = !updateFlag;
	}

	@Override
	protected void syncToClient() {
		if (this.UpdatePacketCounter-- <= 0) {
			UpdatePacketCounter = 100;
			if (!worldObj.isRemote) {
				if (tlrail != null) {
					if (tlrail == null)
						return;
					ERC_MessageCoasterStC packet = new ERC_MessageCoasterStC(getEntityId(), this.paramT, this.Speed,
							tlrail.xCoord, tlrail.yCoord, tlrail.zCoord, ModelID, CoasterOptions);
					ERC_PacketHandler.INSTANCE.sendToAll(packet);
				}
			} else {
				// �N���͂��܂ɃT�[�o�[�ƘA���֌W�̓��������݂�
				if (parent == null) {
					ERC_MessageRequestConnectCtS packet = new ERC_MessageRequestConnectCtS(
							Minecraft.getMinecraft().thePlayer.getEntityId(), this.getEntityId());
					ERC_PacketHandler.INSTANCE.sendToServer(packet);
//					ERC_Logger.info("request sync from client");
				}
			}
		}
	}

	@Override
	protected boolean updateInit() {
		if (parent == null) {
			if (!worldObj.isRemote) { // �I�͐e��T��
				ERC_ManagerCoasterLoad.searchParent(this.getEntityId(), connectIndex, parentuuid);
			}
			return true;

		}
		if (parent.isDead) {
			killCoaster();
			return true;
		}
		if (tlrail == null) {
			return true;
		}

		return super.updateInit();
	}

	protected void updateParamT() {
		parent.updateParamTFirstOnTick();
		Speed = parent.sumSpeed;
		// paramT += Speed / tlrail.Length;

		tlrail = parent.tlrail;

		if (tlrail == null)
			return;
		paramT = parent.paramT - distanceToParent / tlrail.Length;
		// if(worldObj.isRemote)ERC_Logger.info("paramT:"+paramT+"..."+this.getClass().getName());
	}

	@Override
	protected void updateSpeedAndRot() {
//		if (AdjustParamT())
//			return;
		super.updateSpeedAndRot();
		// Speed *= 0.9985;
		// Speed += 0.027 * tlrail.CalcRailPosition2(paramT, ERCPosMat,
		// rotationViewYaw, rotationViewPitch,
		// (riddenByEntity instanceof EntityPlayerMP && worldObj.isRemote),
		// CoasterOptions.SeatHeight);
		//
		// rotationRoll = ERCPosMat.viewRoll;
		// rotationPitch = ERCPosMat.viewPitch;
		// rotationYaw = ERCPosMat.viewYaw;
		//
		// prevRotationYaw = fixrot(rotationYaw, prevRotationYaw);
		// prevRotationPitch = fixrot(rotationPitch, prevRotationPitch);
		// prevRotationRoll = fixrot(rotationRoll, prevRotationRoll);
		//
		// ERCPosMat.prevYaw = fixrot(ERCPosMat.yaw, ERCPosMat.prevYaw);
		// ERCPosMat.prevPitch = fixrot(ERCPosMat.pitch, ERCPosMat.prevPitch);
		// ERCPosMat.prevRoll = fixrot(ERCPosMat.roll, ERCPosMat.prevRoll);
		//
		// tlrail.SpecialRailProcessing(this);
		//
		// // t�����ɍ��W�X�V
		// this.setPosition(ERCPosMat.Pos.xCoord, ERCPosMat.Pos.yCoord,
		// ERCPosMat.Pos.zCoord);
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
		super.readEntityFromNBT(nbt);
		distanceToParent = nbt.getFloat("distancetoparent");
		connectIndex = nbt.getInteger("connectidx");
		parentuuid = UUID.fromString(nbt.getString("parentuuid"));

		ERC_ManagerCoasterLoad.registerChildCoaster(this);

	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
		super.writeEntityToNBT(nbt);
		nbt.setFloat("distancetoparent", distanceToParent);
		nbt.setInteger("connectidx", connectIndex);
		if (parent != null)
			nbt.setString("parentuuid", parent.getUniqueID().toString());
	}

	public void SyncCoasterMisc_Send(ByteBuf buf, int flag) {
		switch (flag) {
		// case 1 : //killcoaster super�Őݒ�
		case 2: // connect coaster to child
			// �e�Ǝ�����ID���M
			buf.writeInt((parent == null) ? -1 : parent.getEntityId()); // �e����Ɏq�����[�h���ꂽ�ꍇ�N�����瓯���v���������Ƃ��ɐe���������Ƃ�����
																		// -1�𑗂��Č�����
			buf.writeInt(connectIndex);
			buf.writeFloat(distanceToParent);
			return;
		}
		super.SyncCoasterMisc_Send(buf, flag);
	}

	public void SyncCoasterMisc_Receive(ByteBuf buf, int flag) {
		switch (flag) {
		case 2: // connect coaster to parent
			if (parent != null)
				return; // �e�������킩���Ă�Ȃ���Ȃ��Ă���
			int parentid = buf.readInt();
			if (parentid == -1)
				return; // �I�̃R�[�X�^�[���e���������ĂȂ����瓯�����~
			int idx = buf.readInt();
			this.connectIndex = idx;
			this.distanceToParent = buf.readFloat();
			ERC_EntityCoaster parent = (ERC_EntityCoaster) worldObj.getEntityByID(parentid);
			if (parent == null)
				return;// �e���܂����[�h����ĂȂ��ƂȂ�H(����Ȃ΂���)

			parent.connectionCoaster(this);
			return;
		}
		super.SyncCoasterMisc_Receive(buf, flag);
	}

	public void receiveConnectionRequestFromClient(int playerid) {
		ERC_PacketHandler.INSTANCE.sendToAll(new ERC_MessageCoasterMisc(this, 2));
//		ERC_Logger.info("ERC_EntityCoasterConnector::recieveConnectionRequestFromClient, request sync do, packet return");
	}

}
