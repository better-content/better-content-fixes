package io.github.bcfixes.mixin.distanthorizons;

import io.github.bcfixes.config.BcFixesConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Semaphore;

@Mixin(targets = "com.seibel.distanthorizons.core.multiplayer.client.AbstractFullDataNetworkRequestQueue", remap = false)
public abstract class AbstractFullDataNetworkRequestQueueMixin {
    private static final String REQUEST_REJECTED_EXCEPTION = "com.seibel.distanthorizons.core.network.exceptions.RequestRejectedException";

    @Shadow
    private Semaphore pendingTasksSemaphore;

    @Inject(method = "handleNetResponse", at = @At("HEAD"), cancellable = true, remap = false)
    private void bcfixes$cancelRejectedStaleRequest(@Coerce Object task, @Coerce Object response, Throwable throwable, CallbackInfo ci) {
        if (!BcFixesConfig.lostCitiesCancelStaleDhClientRequests()
                || throwable == null
                || !REQUEST_REJECTED_EXCEPTION.equals(throwable.getClass().getName())
                || !isDimensionSwitchRejection(throwable)) {
            return;
        }

        pendingTasksSemaphore.release();
        CompletableFuture<?> future = getTaskFuture(task);
        if (future != null) {
            future.cancel(false);
        }
        ci.cancel();
    }

    private static boolean isDimensionSwitchRejection(Throwable throwable) {
        String message = throwable.getMessage();
        return message != null
                && message.contains("Generation not allowed")
                && message.contains("player dimension")
                && message.contains("handler dimension");
    }

    private static CompletableFuture<?> getTaskFuture(Object task) {
        try {
            Field futureField = task.getClass().getField("future");
            Object future = futureField.get(task);
            if (future instanceof CompletableFuture<?> completableFuture) {
                return completableFuture;
            }
        } catch (ReflectiveOperationException ignored) {
        }
        return null;
    }
}
