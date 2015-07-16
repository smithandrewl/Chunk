package edu.missouri.chunk;

import java.net.URI;

/**
 * Sends data of a specified size to a URI at a regular interval.
 *
 * @author Andrew Smith
 */
public class DataTransmitter {
    /**
     * Represents the states of the transmitter
     */
    private enum State {
        STARTED,
        STOPPED
    }

    private State state = State.STOPPED;
    private URI uri;
    private int size;
    private int freq;

    /**
     *  Starts sending data.
     *
     * @throws IllegalStateException Thrown if the transmitter is already running or the uri is null
     */
   public void start() throws IllegalStateException {
       if(isRunning()) {
           throw new IllegalStateException("DataTransmitter is already started");
       }

       if(uri == null) {
           throw new IllegalStateException("URI cannot be null");
       } else if(size == 0) {
           throw new IllegalStateException("Size cannot be null");
       } else if(freq == 0) {
           throw new IllegalStateException("Frequency cannot be null");
       }

       state = State.STARTED;

       enableAlarm();
   }

// ************* TODO: Implement
    private void enableAlarm() {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void disableAlarm() {
        throw new UnsupportedOperationException("Not implemented");
    }
// ************

    /**
     * Stops sending data.
     *
     * @throws IllegalStateException Thrown if the transmitter is already stopped
     */
    public void stop() throws IllegalStateException {
        if(!isRunning()) {
            throw new IllegalStateException("DataTransmitter is already stopped");
        }

        state = State.STOPPED;

        disableAlarm();
    }

    /**
     *
     * @return The URI that the data is being sent to
     */
    public URI getUri() {
        return uri;
    }

    /**
     *
     * @param  uri                   The URI to send data to
     * @throws IllegalStateException Thrown if the transmitter is already running
     */
    public void setUri(URI uri) throws IllegalStateException {
       verifyNotRunning();

        this.uri = uri;
    }

    /**
     *
     * @return The size of the data that is sent to the URI at each interval
     */
    public int getSize() {
        return size;
    }

    /**
     *
     * @param  size                  The size of the data to send to the URI at each interval
     * @throws IllegalStateException Thrown if the transmitter is already running
     */
    public void setSize(int size) throws IllegalStateException {
        verifyNotRunning();

        this.size = size;
    }

    /**
     *
     * @return The frequency at which data will be sent
     */
    public int getFreq() {
        return freq;
    }

    /**
     *
     * @param  freq                  The frequency at which to send data
     * @throws IllegalStateException Thrown if the transmitter is already running
     */
    public void setFreq(int freq) throws IllegalStateException {
        verifyNotRunning();

        this.freq = freq;
    }

    /**
     *
     * @return Returns true if the transmitter is running
     */
    public boolean isRunning() {
        return state == State.STARTED;
    }

    /**
     * Throws an IllegalStateException if the transmitter is already running.
     */
    private void verifyNotRunning() {
        if (isRunning()) {
            throw new IllegalStateException("Cannot mutate DataTransmitter while it is running");
        }
    }
}
