package miyucomics.hexpose.patterns.instance_data

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import net.minecraft.server.world.ServerWorld

class OpGetWorldData(private val process: (ServerWorld) -> List<Iota>) : ConstMediaAction {
	override val argc = 0
	override fun execute(args: List<Iota>, env: CastingEnvironment) = process(env.world)
}