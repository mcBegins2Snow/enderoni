package fluke.end.world.feature;

import java.util.Random;

import fluke.end.util.MathUtils;

import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.gen.feature.WorldGenAbstractTree;

public class WorldGenEnderCanopy extends WorldGenAbstractTree  
{
	protected static final IBlockState LOG = Blocks.WOOL.getDefaultState();
	protected static final IBlockState LEAF = Blocks.WOOL.getStateFromMeta(2);

	public WorldGenEnderCanopy(boolean notify) 
	{
		super(notify);

	}

	@Override
	public boolean generate(World world, Random rand, BlockPos pos) 
	{
		int trunkHeight = 17 + rand.nextInt(7);
		buildTrunk(world, rand, pos, trunkHeight);
		buildBranches(world, rand, pos, trunkHeight);
		return true;
	}
	
	private void buildTrunk(World world, Random rand, BlockPos center, int height)
	{
		//int thiccness = 11;
		int trunkCore = 5;
		int trunkDecoWidth = 7;
		for(int x=-trunkCore; x<=trunkCore; x++)
		{
			for(int z=-trunkCore; z<=trunkCore; z++)
			{
				int colHeight;
				
				if(Math.abs(x)<=1 && Math.abs(z)<=1)
				{
					colHeight = height;
				}
				else if(Math.abs(x)<=Math.abs(z))
				{
					colHeight = 14 - Math.abs(x) - Math.abs(z)*2 - rand.nextInt(2);
				}
				else
				{
					colHeight = 14 - Math.abs(x)*2 - Math.abs(z) - rand.nextInt(2);
				}
				
				for(int y=0; y<colHeight; y++)
				{
					placeLogAt(world, center.add(x, y, z));
				}
			}
		}
	}

	private void buildBranches(World world, Random rand, BlockPos center, int trunkHeight)
	{
		double xAngleTranslation = 0.7071; //Math.cos(Math.toRadians(45));
		double zAngleTranslation = 0.7071; //Math.sin(Math.toRadians(45));
		int[][] dirArray = {{1,1}, {-1,1}, {1,-1}, {-1,-1}};
		int branchLength = 16 + rand.nextInt(8);
		int branchHeight = 8 + rand.nextInt(7);
		center = center.add(0, trunkHeight, 0);

		for(int n=0; n<4; n++)
		{
			int xOffset = dirArray[n][0];
			int zOffset = dirArray[n][1];
			
			//current CurvedBresehnam only works in 2d, ignore z axis
			BlockPos branchEnd = center.add(branchLength, branchHeight, 0);
			BlockPos branchCurve = center.add(branchLength/3, branchHeight, 0);
			BlockPos branchStart = center.add(xOffset, 0, zOffset);
			BlockPos[] branchArray = MathUtils.getQuadBezierArray(branchStart, branchCurve, branchEnd);
			for(BlockPos pos : branchArray)
			{
				
				int pxXoffset = pos.getX() - branchStart.getX();
				int pxYoffset = pos.getY() - branchStart.getY();
				int pxZoffset = pos.getZ() - branchStart.getZ();
				int pxDistance = pxXoffset; 
				
				//get x, z positions for branches at 45 degrees
				int angledX = (int)(pxDistance * xAngleTranslation + 0.5);
				int angledZ = (int)(pxDistance * zAngleTranslation + 0.5);

				placeLogAt(world, branchStart.add(angledX*xOffset, pxYoffset, angledZ*zOffset));
				
				if(pxDistance == branchLength - 3)// && rand.nextBoolean()) 
				{//not working
					BlockPos tipBranchStart = branchStart.add(angledX*xOffset, pxYoffset, angledZ*zOffset);
					BlockPos tipBranchEnd = tipBranchStart.add(0, 8, 0);
					BlockPos tipBranchCurve = tipBranchStart.add(0, 4, 0);
					BlockPos[] tipBranchArray = MathUtils.getQuadBezierArray(tipBranchStart, tipBranchCurve, tipBranchEnd);
					for(BlockPos pos2 : tipBranchArray)
					{
						System.out.println(xOffset + " " + zOffset);
						int pxXoffset2 = pos2.getX() - tipBranchStart.getX();
						int pxYoffset2 = pos2.getY() - tipBranchStart.getY();
						int pxZoffset2 = pos2.getZ() - tipBranchStart.getZ();
						BlockPos tipLogPos = new BlockPos(pos2.getX(), tipBranchStart.getY(), pos2.getY());
						placeLogAt(world, pos2);
					}
				}
			}
		}
	}
	
	private void placeLogAt(World worldIn, BlockPos pos)
    {
        this.setBlockAndNotifyAdequately(worldIn, pos, LOG);
    }
	
	private void placeLeafAt(World worldIn, BlockPos pos)
    {
        IBlockState state = worldIn.getBlockState(pos);

        if (state.getBlock().isAir(state, worldIn, pos) || state.getBlock().isLeaves(state, worldIn, pos))
        {
            this.setBlockAndNotifyAdequately(worldIn, pos, LEAF);
        }
    }
	
	//Draws a line from start to end with midsection pulled towards curvePos
	protected void drawCurvedBresehnam(World world, BlockPos start, BlockPos end, BlockPos curvePos, IBlockState state) 
	{
		for (BlockPos pixel : MathUtils.getQuadBezierArray(start, curvePos, end)) 
		{
			this.setBlockAndNotifyAdequately(world, pixel, state);
		}
	}

}