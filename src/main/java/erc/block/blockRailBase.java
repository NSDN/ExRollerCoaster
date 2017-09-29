package erc.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import erc._core.ERC_Core;
import erc.manager.ERC_CoasterAndRailManager;
import erc.message.ERC_MessageConnectRailCtS;
import erc.message.ERC_PacketHandler;
import erc.tileEntity.TileEntityRailBase;
import erc.tileEntity.Wrap_TileEntityRail;
import net.minecraft.block.Block;
import net.minecraft.block.BlockContainer;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.IIcon;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class blockRailBase extends BlockContainer{
	
	public blockRailBase()
	{
		super(Material.ground);
		this.setHardness(0.3F);
		this.setResistance(2000.0F);
		this.setLightOpacity(0);
		this.setBlockBounds(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		this.setLightLevel(0.6F);//0.6
	}
 
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int par1, int par2)
	{
		return super.getIcon(par1,par2);
	}
 
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister par1IconRegister){}
 
	@Override
	public int getRenderType()
	{
		return ERC_Core.blockRailRenderId; // RenderBlockRail�p
	}
 
	@Override
	public boolean isOpaqueCube()
	{
		return false;
	}
 
	public boolean renderAsNormalBlock()
	{
		return false;
	}
	
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_)
    {
        return false;
    }
	
	// Args: World, X, Y, Z, side, hitX, hitY, hitZ, block metadata
	@Override
	public int onBlockPlaced(World world, int x, int y, int z, int side, float hitX, float hitY, float hitZ, int meta)
	{
		meta = side;
		return side;
		//return super.onBlockPlaced(world, x, y, z, side, hitX, hitY, hitZ, meta);
	}
	
	public abstract Wrap_TileEntityRail getTileEntityInstance();

	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase player, ItemStack p_149689_6_)
	{
//		Wrap_TileEntityRail WPrevTile = ERC_BlockRailManager.GetPrevTileEntity(world);
//		Wrap_TileEntityRail WNextTile = ERC_BlockRailManager.GetNextTileEntity(world);
		Wrap_TileEntityRail tlRailTest = getTileEntityInstance();
//		ERC_TileEntityRailTest tlRailTest = (ERC_TileEntityRailTest)world.getTileEntity(x, y, z);
		
		/**
		 * ////////////// �N���C�A���g��Manager�ɐݒ�ƃp�P�b�g���M
		*/
		if(world.isRemote) 
		{
			if(ERC_CoasterAndRailManager.isPlacedPrevRail())
			{
				// �O��u�������[��������΃T�[�o�[�ɕ񍐁E�A���v��
				ERC_MessageConnectRailCtS packet 
					= new ERC_MessageConnectRailCtS(
							ERC_CoasterAndRailManager.prevX, ERC_CoasterAndRailManager.prevY, ERC_CoasterAndRailManager.prevZ,
							x, y, z
							);
				ERC_PacketHandler.INSTANCE.sendToServer(packet);
			}
			
			// ���u�������[�����L�^
			ERC_CoasterAndRailManager.SetPrevData(x, y, z);

			if(ERC_CoasterAndRailManager.isPlacedNextRail())
			{
				// �O��폜�������[���̐�Ɏ��̃��[�����q�����Ă���΃T�[�o�[�ɕ񍐁E�A���v��
				ERC_MessageConnectRailCtS packet 
					= new ERC_MessageConnectRailCtS(
							x, y, z,
							ERC_CoasterAndRailManager.nextX, ERC_CoasterAndRailManager.nextY, ERC_CoasterAndRailManager.nextZ
							);
				ERC_PacketHandler.INSTANCE.sendToServer(packet);

				// ����ɐ�Ƀ��[������������̂ŁA�ۑ����[�����͍폜
				ERC_CoasterAndRailManager.ResetData();
		 	}
			
			return;
		}
		
		/**
		 * ////////////// �T�[�o�[�̓��[���ݒ�v�Z
		*/
		super.onBlockPlacedBy(world, x, y, z, player, p_149689_6_); 
		//tlRailTest.myisInvalid(); // ?

		onTileEntityInitFirst(world, player, tlRailTest, x, y, z);
		
		world.setTileEntity(x, y, z, tlRailTest);
//		tlRailTest.onTileSetToWorld_Init();
		tlRailTest.syncData();
		
//		// �O���[�����ݒu����Ă�����A���̏������ɑO��ݒu�������[����TileEntity�̍��W�ݒ� ... 8/11:�N���C�A���g����̃p�P�b�g�������������
		// �R�[�h��ERC_MessageRailConnectRailCtS��

		// ���[����ݒu������BlockRailManager�ɓo�^
//		ERC_BlockRailManager.SetPrevData(x, y, z);
	}
	
	protected void onTileEntityInitFirst(World world, EntityLivingBase player, Wrap_TileEntityRail Wrail, int x, int y, int z)
	{
		TileEntityRailBase rail = Wrail.getRail();
		// �u���b�N�ݒu���̃v���C���[�̌���
		double yaw = Math.toRadians(player.rotationYaw);
		double pit = -Math.toRadians(player.rotationPitch);
		Vec3 metadir = ConvertVec3FromMeta(world.getBlockMetadata(x, y, z));
		Vec3 vecDir = Vec3.createVectorHelper(
				-Math.sin(yaw) * (metadir.xCoord!=0?0:1), 
				Math.sin(pit) * (metadir.yCoord!=0?0:1), 
				Math.cos(yaw) * (metadir.zCoord!=0?0:1) );
		
		// �V�K�ݒu�̃��[���ɑ΂��č��W�ݒ�B�@�����̓v���C���[�̌����Ă��������
		rail.SetBaseRailPosition(x, y, z, vecDir, metadir, 15f);
//		rail.SetNextRailPosition(x+(int)(vecDir.xCoord*10), y+(int)(vecDir.yCoord*10), z+(int)(vecDir.zCoord*10));
		rail.SetNextRailVectors(
				Vec3.createVectorHelper(x+(int)(vecDir.xCoord*10)+0.5, y+(int)(vecDir.yCoord*10)+0.5, z+(int)(vecDir.zCoord*10)+0.5), 
				vecDir, 
				rail.getRail().BaseRail.vecUp, 
				0f, 0f,
				rail.getRail().BaseRail.Power,
				-1, -1, -1);
		rail.CalcRailLength(); 
		rail.Init();
	}

	// �T�[�o�[�̂݁@Manager�̓o�^���ύX onBlockDestroyByPlayer����ɌĂ΂��B�@����super��TileEntity���폜���Ă��邽�߁A��������TileEntity����O���[���̏��擾�AManager�̓o�^���ύX
	@Override
	public void breakBlock(World world, int x, int y, int z, Block block, int p_149749_6_)
	{
		TileEntityRailBase thistl = ((Wrap_TileEntityRail)(world.getTileEntity(x, y, z))).getRail();
		if(thistl!=null)
		{
			thistl.setBreak(true);
			Wrap_TileEntityRail prev = thistl.getPrevRailTileEntity();
			if(prev!=null)prev.getRail().NextRail.SetPos(-1, -1, -1);
			Wrap_TileEntityRail next = thistl.getNextRailTileEntity();
			if(next!=null)next.getRail().BaseRail.SetPos(-1, -1, -1);
		}
		super.breakBlock(world, x, y, z, block, p_149749_6_);
	}

//	@Override
//	public void harvestBlock(World world, EntityPlayer player, int x, int y, int z, int p_149636_6_) 
//	{	
////		ERC_MessageSaveBreakRailStC packet = new ERC_MessageSaveBreakRailStC(
////				ERC_BlockRailManager.prevX, ERC_BlockRailManager.prevY, ERC_BlockRailManager.prevZ,
////				ERC_BlockRailManager.nextX, ERC_BlockRailManager.nextY, ERC_BlockRailManager.nextZ
////				);
////		ERC_PacketHandler.INSTANCE.sendTo(packet,(EntityPlayerMP) player);
//		super.harvestBlock(world, player, x, y, z, p_149636_6_);
//	}

	//	// �u���b�N���j�󂳂ꂽ��Ă΂��@
	public void onBlockDestroyedByPlayer(World world, int x, int y, int z, int i)
	{	
//		if(!Minecraft.getMinecraft().thePlayer.capabilities.isCreativeMode)
//			this.dropBlockAsItem(world, x, y, z, new ItemStack(this));
	}

	//�����_�[�Ŏg������g��Ȃ�������
	public void setBlockBoundsForItemRender()
	{
		//this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.4F, 1.0F);
	}
 
	//�K�i��n�[�t�u���b�N�݂�Ƃ�������
	public void setBlockBoundsBasedOnState(IBlockAccess par1IBlockAccess, int x, int y, int z)
	{
		//super.setBlockBoundsBasedOnState(par1IBlockAccess, par2, par3, par4);
		//this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
		int meta = par1IBlockAccess.getBlockMetadata(x, y, z);
		SetBlockBoundsUsingMeta(meta);
	}
 
	//�����蔻��B�T�{�e����\�E���T���h���Q�l�ɂ���Ɨǂ��B�R�R�̐ݒ������ƁAonEntityCollidedWithBlock���Ă΂��悤�ɂȂ�
	public AxisAlignedBB getCollisionBoundingBoxFromPool(World par1World, int x, int y, int z)
	{
		return AxisAlignedBB.getBoundingBox(
				((double)x)+this.minX,((double)y)+this.minY,((double)z)+this.minZ,
				((double)x)+this.maxX,((double)y)+this.maxY,((double)z)+this.maxZ
				);
	}
//	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB aabb, List list, Entity entity)
//    {
//        if (flag || flag1)
//        {
//            this.setBlockBounds(
//            		((double)x)+this.minX,((double)y)+this.minY,((double)z)+this.minZ,
//    				((double)x)+this.maxX,((double)y)+this.maxY,((double)z)+this.maxZ
//    				);
//            super.addCollisionBoxesToList(world, x, y, z, aabb, list, entity);
//        }
//        this.setBlockBounds(f, 0.0F, f2, f1, 1.0F, f3);
//    }
 
	//�u���b�N�Ɏ��_�����킹�����ɏo�Ă��鍕�����̃A��
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getSelectedBoundingBoxFromPool(World par1World, int x, int y, int z)
	{
		return AxisAlignedBB.getBoundingBox(
				((double)x)+this.minX,((double)y)+this.minY,((double)z)+this.minZ,
				((double)x)+this.maxX,((double)y)+this.maxY,((double)z)+this.maxZ
				);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int meta)
	{
		return getTileEntityInstance();
	}
	
	public static boolean isBlockRail(Block block)
	{
		 return block instanceof blockRailBase;
	}
    
    private void SetBlockBoundsUsingMeta(int meta)
    {
    	switch(meta&7){
		case 0: // ��
			this.setBlockBounds(0.2F, 0.7F, 0.2F, 0.8F, 1.0F, 0.8F);
            break;
		case 1: // ��
			this.setBlockBounds(0.2F, 0.0F, 0.2F, 0.8F, 0.3F, 0.8F);
            break;
		case 2: // ��
			this.setBlockBounds(0.2F, 0.2F, 0.7F, 0.8F, 0.8F, 1.0F);
            break;
		case 3: // �k
			this.setBlockBounds(0.2F, 0.2F, 0.0F, 0.8F, 0.8F, 0.3F);
            break;
		case 4: // ��
			this.setBlockBounds(0.7F, 0.2F, 0.2F, 1.0F, 0.8F, 0.8F);
            break;
		case 5: // ��
			this.setBlockBounds(0.0F, 0.2F, 0.2F, 0.3F, 0.8F, 0.8F);
            break;
        default:
        	this.setBlockBounds(0.4F, 0.4F, 0.4F, 0.6F, 0.6F, 0.6F);
            break;
		}
    }
    
    protected Vec3 ConvertVec3FromMeta(int meta)
    {
    	switch(meta&7){
    	case 0:return Vec3.createVectorHelper(0, -1, 0);
    	case 1:return Vec3.createVectorHelper(0, 1, 0);
    	case 2:return Vec3.createVectorHelper(0, 0, -1);
    	case 3:return Vec3.createVectorHelper(0, 0, 1);
    	case 4:return Vec3.createVectorHelper(-1, 0, 0);
    	case 5:return Vec3.createVectorHelper(1, 0, 0);
    	}
		return Vec3.createVectorHelper(0, 0, 0);
    }
    
    
    
}
