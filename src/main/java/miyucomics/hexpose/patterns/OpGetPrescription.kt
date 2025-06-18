package miyucomics.hexpose.patterns

import at.petrak.hexcasting.api.casting.asActionResult
import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.EntityIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import miyucomics.hexpose.iotas.IdentifierIota
import miyucomics.hexpose.iotas.ItemStackIota
import miyucomics.hexpose.iotas.asActionResult
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.entity.projectile.ArrowEntity
import net.minecraft.entity.projectile.ShulkerBulletEntity
import net.minecraft.entity.projectile.WitherSkullEntity
import net.minecraft.entity.projectile.thrown.PotionEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.potion.PotionUtil
import net.minecraft.registry.Registries

class OpGetPrescription : ConstMediaAction {
	override val argc = 1
	override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
		return when (val arg = args[0]) {
			is EntityIota -> {
				env.assertEntityInRange(arg.entity)
				when (val entity = arg.entity) {
					is ItemEntity -> handleItemStack(entity.stack, args)
					is ArrowEntity -> {
						val output = mutableListOf<IdentifierIota>()
						for (instance in entity.potion.effects)
							output.add(IdentifierIota(Registries.STATUS_EFFECT.getId(instance.effectType)!!))
						for (instance in entity.effects)
							output.add(IdentifierIota(Registries.STATUS_EFFECT.getId(instance.effectType)!!))
						output
					}
					is PotionEntity -> {
						val effects = mutableListOf<IdentifierIota>()
						for (effect in PotionUtil.getPotionEffects(entity.stack))
							effects.add(IdentifierIota(Registries.STATUS_EFFECT.getId(effect.effectType)!!))
						effects
					}
					is ShulkerBulletEntity -> Registries.STATUS_EFFECT.getId(StatusEffects.LEVITATION)!!.asActionResult()
					is WitherSkullEntity -> Registries.STATUS_EFFECT.getId(StatusEffects.WITHER)!!.asActionResult()
					else -> listOf()
				}.asActionResult
			}
			is ItemStackIota ->  handleItemStack(arg.stack, args)
			else -> throw MishapInvalidIota.of(args[0], 0, "potion_holding")
		}
	}

	companion object {
		private fun handleItemStack(stack: ItemStack, args: List<Iota>): List<IdentifierIota> {
			if (!(stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION) || stack.item.isFood || stack.isOf(Items.TIPPED_ARROW)))
				throw MishapInvalidIota.of(args[0], 0, "potion_holding")
			val effects = mutableListOf<IdentifierIota>()
			if (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION)) {
				for (effect in PotionUtil.getPotionEffects(stack))
					effects.add(IdentifierIota(Registries.STATUS_EFFECT.getId(effect.effectType)!!))
			} else if (stack.isOf(Items.TIPPED_ARROW)) {
				for (effect in PotionUtil.getPotion(stack).effects)
					effects.add(IdentifierIota(Registries.STATUS_EFFECT.getId(effect.effectType)!!))
				for (effect in PotionUtil.getCustomPotionEffects(stack))
					effects.add(IdentifierIota(Registries.STATUS_EFFECT.getId(effect.effectType)!!))
			} else {
				for (statusEffect in stack.item.foodComponent!!.statusEffects)
					effects.add(IdentifierIota(Registries.STATUS_EFFECT.getId(statusEffect.first.effectType)!!))
			}
			return effects
		}
	}
}