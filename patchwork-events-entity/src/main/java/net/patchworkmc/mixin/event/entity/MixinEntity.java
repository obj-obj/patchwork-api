/*
 * Minecraft Forge, Patchwork Project
 * Copyright (c) 2016-2020, 2019-2020
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation version 2.1
 * of the License.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package net.patchworkmc.mixin.event.entity;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityDimensions;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.EntityType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import net.patchworkmc.impl.event.entity.EntityEvents;

@Mixin(Entity.class)
public abstract class MixinEntity {
	@Shadow
	private float standingEyeHeight;

	@Shadow
	private EntityDimensions dimensions;

	@Shadow
	protected abstract float getEyeHeight(EntityPose pose, EntityDimensions dimensions);

	@Shadow
	private Entity vehicle;

	@Inject(method = "<init>", at = @At("RETURN"))
	public void hookConstructor(EntityType<?> type, World world, CallbackInfo ci) {
		Entity entity = (Entity) (Object) this;

		this.standingEyeHeight = EntityEvents.getEyeHeight(entity, EntityPose.STANDING, dimensions, getEyeHeight(EntityPose.STANDING, dimensions));

		EntityEvents.onEntityConstruct(entity);
	}

	@Inject(method = "startRiding(Lnet/minecraft/entity/Entity;Z)Z",
			at = @At(value = "JUMP",
					opcode = Opcodes.IFNE,
					ordinal = 0
			)
	)
	public void onStartRiding(Entity entity, boolean force, CallbackInfoReturnable<Boolean> ci) {
		Entity thisEntity = (Entity) (Object) this;

		if (!EntityEvents.canMountEntity(thisEntity, entity, true)) {
			ci.setReturnValue(false);
		}
	}

	@Inject(method = "changeDimension",
			at = @At("HEAD"),
			cancellable = true
	)
	void patchwork_fireTravelToDimensionEventChangeDimension(DimensionType newDimension, CallbackInfoReturnable<Entity> cir) {
		if (!EntityEvents.onTravelToDimension((Entity) (Object) this, newDimension)) {
			cir.setReturnValue(null);
		}
	}
}
