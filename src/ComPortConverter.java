import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.openmuc.jrxtx.DataBits;
import org.openmuc.jrxtx.FlowControl;
import org.openmuc.jrxtx.Parity;
import org.openmuc.jrxtx.SerialPort;
import org.openmuc.jrxtx.StopBits;

public class ComPortConverter implements SerialPort {
	
	private com.fazecast.jSerialComm.SerialPort comPort;
	
	public ComPortConverter(com.fazecast.jSerialComm.SerialPort comPort) {
		this.comPort = comPort;
	}

	@Override
	public void close() throws IOException {
		this.comPort.closePort();
	}

	@Override
	public int getBaudRate() {
		return this.comPort.getBaudRate();
	}

	@Override
	public DataBits getDataBits() {
		return null;
	}

	@Override
	public FlowControl getFlowControl() {
		return null;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return this.comPort.getInputStream();
	}

	@Override
	public OutputStream getOutputStream() throws IOException {
		return this.comPort.getOutputStream();
	}

	@Override
	public Parity getParity() {
		return null;
	}

	@Override
	public String getPortName() {
		// TODO Auto-generated method stub
		return this.comPort.getSystemPortName();
	}

	@Override
	public int getSerialPortTimeout() {
		return this.comPort.getReadTimeout();
	}

	@Override
	public StopBits getStopBits() {
		return null;
	}

	@Override
	public boolean isClosed() {
		return !this.comPort.isOpen();
	}

	@Override
	public void setBaudRate(int arg0) throws IOException {
		this.comPort.setBaudRate(arg0);
	}

	@Override
	public void setDataBits(DataBits arg0) throws IOException {
		
	}

	@Override
	public void setFlowControl(FlowControl arg0) throws IOException {
		
	}

	@Override
	public void setParity(Parity arg0) throws IOException {
		
	}

	@Override
	public void setSerialPortTimeout(int arg0) throws IOException {
		
	}

	@Override
	public void setStopBits(StopBits arg0) throws IOException {
		
	}

}
