import com.osfocus.tank.Dir;
import com.osfocus.tank.net.*;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TankDirChangedCodecMsgtest {
    @Test
    void testEncode() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ch.pipeline().addLast(new MsgEncoder());
        Msg msg = new TankDirChangedMsg(UUID.randomUUID(),10, 15, Dir.DOWN);
        ch.writeOutbound(msg);

        ByteBuf buf = (ByteBuf) ch.readOutbound();
        MsgType msgType = MsgType.values()[buf.readInt()];
        int length = buf.readInt();

        UUID id = new UUID(buf.readLong(), buf.readLong());
        int x = buf.readInt();
        int y = buf.readInt();
        Dir dir = Dir.values()[buf.readInt()];

        assertEquals(msgType, MsgType.TankDirChanged);
        assertEquals(12, length);
        assertEquals(10, x);
        assertEquals(15, y);
        assertEquals(dir, Dir.DOWN);
    }

    @Test
    void testDecode() {
        EmbeddedChannel ch = new EmbeddedChannel();
        ch.pipeline().addLast(new MsgDecoder());
        Msg msg = new TankDirChangedMsg(UUID.randomUUID(),10, 15, Dir.DOWN);

        ByteBuf buf = Unpooled.buffer();
        buf.writeInt(msg.getMsgType().ordinal());
        byte[] bytes = msg.toBytes();
        buf.writeInt(bytes.length);
        buf.writeBytes(bytes);
        ch.writeInbound(buf.duplicate());

        TankDirChangedMsg newMsg = (TankDirChangedMsg) ch.readInbound();
        assertEquals(10, newMsg.x);
        assertEquals(15, newMsg.y);
        assertEquals(Dir.DOWN, newMsg.dir);
    }
}
