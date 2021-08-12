package com.enderio.core.client;

import javax.annotation.Nonnull;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import java.util.Arrays;

// TODO: Stop using in ClientProxy and make abstract.
@OnlyIn(Dist.CLIENT)
public class ModCompatibilityException extends RuntimeException {

  private final String[] msgs;

  public ModCompatibilityException(@Nonnull String[] msgs) {
    this.msgs = msgs;
  }

  @Override
  public String getMessage() {
    if (msgs.length == 0)
      return super.getMessage();

    StringBuilder context = new StringBuilder(msgs[0]);
    Arrays.stream(msgs).skip(1).forEach(msg -> context.append("\n"+msg));
    return context.toString();
  }

  @Override
  public void printStackTrace() {
    super.printStackTrace();
  }
}
