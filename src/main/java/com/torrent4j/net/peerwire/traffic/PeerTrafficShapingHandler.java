package com.torrent4j.net.peerwire.traffic;

import io.netty.handler.traffic.ChannelTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;

import java.util.concurrent.Executor;

import com.torrent4j.model.TorrentPeer;
import com.torrent4j.model.TorrentPeerTrafficControl;

public class PeerTrafficShapingHandler extends ChannelTrafficShapingHandler {
	private long writeLimit;
	private long readLimit;

	private TorrentPeer peer;

	public PeerTrafficShapingHandler(Executor executor) {
		super(executor, 0, 0);
	}

	private void reconfigure() {
		if (peer == null)
			return;
		final TorrentPeerTrafficControl peerTraffic = peer.getTrafficControl();
		long readLimit = peerTraffic.getDownloadSpeedLimit();
		long writeLimit = peerTraffic.getUploadSpeedLimit();
		if (readLimit != this.readLimit || writeLimit != this.writeLimit) {
			this.writeLimit = writeLimit;
			this.readLimit = readLimit;
			configure(writeLimit, readLimit);
		}
	}

	@Override
	protected void doAccounting(TrafficCounter counter) {
		if (peer == null)
			return;
		reconfigure();
		peer.getTrafficControl().setCurrentDownloadSpeed(
				counter.getLastReadThroughput());
		peer.getTrafficControl().setCurrentUploadSpeed(
				counter.getLastWriteThroughput());
	}

	/**
	 * @return the peer
	 */
	public TorrentPeer getPeer() {
		return peer;
	}

	/**
	 * @param peer
	 *            the peer to set
	 */
	public void setPeer(TorrentPeer peer) {
		this.peer = peer;
		reconfigure();
	}
}