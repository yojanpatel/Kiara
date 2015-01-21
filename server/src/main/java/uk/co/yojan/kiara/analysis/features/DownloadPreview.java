package uk.co.yojan.kiara.analysis.features;

import com.wrapper.spotify.Api;
import com.wrapper.spotify.methods.TrackRequest;
import com.wrapper.spotify.models.Track;
import uk.co.yojan.kiara.server.SpotifyApi;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ShortBuffer;

/**
 * Adapted from JAudio.
 *
 * A holder class for general static methods relating to sampled audio involving
 * classes used in the javax.sound.sampled package. Also includes methods for use
 * in converting back an forth between audio stored using this package and audio
 * stored as samples in arrays of doubles.
 *
 * @author	Cory McKay
 */
public class DownloadPreview {

  private static boolean normalise = true;


  public static double[] download(String id) throws Exception {

    Api api = SpotifyApi.clientCredentialsApi();

    TrackRequest request = api.getTrack(id).build();
    Track track = request.get();
    URLConnection conn = new URL(track.getPreviewUrl()).openConnection();
    InputStream is = conn.getInputStream();
    AudioInputStream original_stream = AudioSystem.getAudioInputStream(is);
    AudioFormat original_format = original_stream.getFormat();

    // Set the bit depth
    int bit_depth = original_format.getSampleSizeInBits();
    if (bit_depth != 8 && bit_depth != 16)
      bit_depth = 16;

    // AudioInputStream new_stream = original_stream;

    AudioFormat new_format = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, original_format.getSampleRate(), bit_depth, original_format.getChannels(), original_format.getChannels() * (bit_depth / 8), original_format.getSampleRate(), true);
    AudioInputStream new_stream = AudioSystem.getAudioInputStream(new_format, original_stream);

    double[][] channel_samples;
    double[] samples = null;

    channel_samples = extractSampleValues(new_stream);
    if (channel_samples.length > 1)
      samples = getSamplesMixedDownIntoOneChannel(channel_samples);
    else if(channel_samples.length == 1)
      samples = channel_samples[0];
    else if(channel_samples.length < 1)
      samples = null;

    if (normalise)
      samples = normalizeSamples(samples);

    return samples;
  }

  /**
   * Returns an array of doubles representing the samples for each channel
   * in the given AudioInputStream.
   *
   * <p>This method is only compatible with audio with bit depths of 8 or 16 bits
   * that is encoded using signed PCM with big endian byte order.
   *
   * @param	audio_input_stream	The AudioInputStream to convert to sample values.
   * @return						A 2-D array of sample values whose first indice indicates
   *								channel and whose second indice indicates sample number.
   *								In stereo, indice 0 corresponds to left and 1 to right.
   *								All samples should fall between -1 and +1.
   * @throws	Exception			Throws an informative exception if an invalid paramter
   *								is provided.
   */
  public static double[][] extractSampleValues(AudioInputStream audio_input_stream)
      throws Exception
  {
    // Converts the contents of audio_input_stream into an array of bytes
    byte[] audio_bytes = getBytesFromAudioInputStream(audio_input_stream);

    // Note the AudioFormat
    AudioFormat this_audio_format = audio_input_stream.getFormat();

    // Extract information from this_audio_format
    int number_of_channels = this_audio_format.getChannels();
    int bit_depth = this_audio_format.getSampleSizeInBits();

    // Throw exception if incompatible this_audio_format provided
    if ( (bit_depth != 16 && bit_depth != 8 )||
        !this_audio_format.isBigEndian() ||
        this_audio_format.getEncoding() != AudioFormat.Encoding.PCM_SIGNED )
      throw new Exception( "Only 8 or 16 bit signed PCM samples with a big-endian\n" +
          "byte order can be analyzed currently." );

    // Find the number of samples in the audio_bytes
    int number_of_bytes = audio_bytes.length;
    int bytes_per_sample = bit_depth / 8;
    int number_samples = number_of_bytes / bytes_per_sample / number_of_channels;

    // Throw exception if incorrect number of bytes given
    if ( ((number_samples == 2 || bytes_per_sample == 2) && (number_of_bytes % 2 != 0)) ||
        ((number_samples == 2 && bytes_per_sample == 2) && (number_of_bytes % 4 != 0)) )
      throw new Exception("Uneven number of bytes for given bit depth and number of channels.");

    // Find the maximum possible value that a sample may have with the given
    // bit depth
    double max_sample_value = findMaximumSampleValue(bit_depth) + 2.0;

    // Instantiate the sample value holder
    double[][] sample_values = new double[number_of_channels][number_samples];

    // Convert the bytes to double samples
    ByteBuffer byte_buffer = ByteBuffer.wrap(audio_bytes);
    if (bit_depth == 8)
    {
      for (int samp = 0; samp < number_samples; samp++)
        for (int chan = 0; chan < number_of_channels; chan++)
          sample_values[chan][samp] = (double) byte_buffer.get() / max_sample_value;
    }
    else if (bit_depth == 16)
    {
      ShortBuffer short_buffer = byte_buffer.asShortBuffer();
      for (int samp = 0; samp < number_samples; samp++)
        for (int chan = 0; chan < number_of_channels; chan++)
          sample_values[chan][samp] = (double) short_buffer.get() / max_sample_value;
    }

    // Return the samples
    return sample_values;
  }

  /**
   * Generates an array of audio bytes based on the contents of the given
   * <code>AudioInputStream</code>. Extracts all of the bytes available in the
   * <code>AudioInputStream</code> at the moment that this method is called.
   *
   * @param	audio_input_stream	The <code>AudioInputStream</code> to extract the
   *								bytes from.
   * @return						The audio bytes extracted from the
   *                              <code>AudioInputStream</code>. Has the same
   *								<code>AudioFileFormat</code> as the specified
   *								<code>AudioInputStream</code>.
   * @throws	Exception			Throws an exception if a problem occurs.
   */
  public static byte[] getBytesFromAudioInputStream(AudioInputStream audio_input_stream)
      throws Exception {
    // Calculate the buffer size to use
    float buffer_duration_in_seconds = 0.25F;
    int buffer_size = getNumberBytesNeeded(buffer_duration_in_seconds,
        audio_input_stream.getFormat());
    byte rw_buffer[] = new byte[buffer_size + 2];

    // Read the bytes into the rw_buffer and then into the ByteArrayOutputStream
    ByteArrayOutputStream byte_array_output_stream = new ByteArrayOutputStream();
    int position = audio_input_stream.read(rw_buffer, 0, rw_buffer.length);
    while (position > -1)
    {
      byte_array_output_stream.write(rw_buffer, 0, position);
      position = audio_input_stream.read(rw_buffer, 0, rw_buffer.length);
    }
    byte[] results = byte_array_output_stream.toByteArray();
    try
    {
      byte_array_output_stream.close();
    }
    catch (IOException e)
    {
      System.out.println(e);
    }

    // Return the results
    return results;
  }


  /**
   * Returns the number of bytes needed to store samples corresponding to audio
   * of fixed duration.
   *
   * @param	duration_in_seconds	The duration, in seconds, of the audio that
   *								needs to be stored.
   * @param	audio_format		The <code>AudioFormat</code> of the samples
   *								to be stored.
   * @return						The number of bytes needed to store the samples.
   */
  public static int getNumberBytesNeeded( double duration_in_seconds,
                                          AudioFormat audio_format )
  {
    int frame_size_in_bytes = audio_format.getFrameSize();
    float frame_rate = audio_format.getFrameRate();
    return (int) (frame_size_in_bytes * frame_rate * duration_in_seconds);
  }

  /**
   * Returns the number of bytes needed to store samples corresponding to
   * the given number of samples in a given <code>AudioFormat</code>.
   *
   * @param	number_samples		The number of samples to be encoded.
   * @param	audio_format		The <code>AudioFormat</code> of the samples
   *								to be stored.
   * @return						The number of bytes needed to store the samples.
   */
  public static int getNumberBytesNeeded( int number_samples,
                                          AudioFormat audio_format ) {
    int number_bytes_per_sample = audio_format.getSampleSizeInBits() / 8;
    int number_channels = audio_format.getChannels();
    return (number_samples * number_bytes_per_sample * number_channels);
  }


  /**
   * Returns the maximum possible value that a signed sample can have under
   * the given bit depth. May be 1 or 2 values smaller than actual max,
   * depending on specifics of encoding used.
   *
   * @param	bit_depth	The bit depth to examine.
   * @return				The maximum possible positive sample value as a double.
   */
  public static double findMaximumSampleValue(int bit_depth) {
    int max_sample_value_int = 1;
    for (int i = 0; i < (bit_depth - 1); i++)
      max_sample_value_int *= 2;
    max_sample_value_int--;
    double max_sample_value = ((double) max_sample_value_int) - 1.0;
    return max_sample_value;
  }

  /* DSPMethods ****/

  /**
   * Returns the given set of samples as a set of samples mixed down into one
   * channel.
   *
   * @param	audio_samples	Audio samles to modify, with a minimum value of
   *							-1 and a maximum value of +1. The first indice
   *							corresponds to the channel and the second indice
   *							corresponds to the sample number.
   * @return					The given audio samples mixed down, with equal
   *							gain, into one channel.
   */
  public static double[] getSamplesMixedDownIntoOneChannel(double[][] audio_samples)
  {
    if (audio_samples.length == 1)
      return audio_samples[0];

    double number_channels = (double) audio_samples.length;
    int number_samples = audio_samples[0].length;

    double[] samples_mixed_down = new double[number_samples];
    for (int samp = 0; samp < number_samples; samp++)
    {
      double total_so_far = 0.0;
      for (int chan = 0; chan < number_channels; chan++)
        total_so_far += audio_samples[chan][samp];
      samples_mixed_down[samp] = total_so_far / number_channels;
    }

    return samples_mixed_down;
  }

  /**
   * Normalizes the given samples so that the absolute value of the highest
   * sample amplitude is 1. Does nothing if also samples are 0.
   *
   * @param	samples_to_normalize	The samples to normalize.
   * @return							Returns a copy of the given samples
   *									after normalization.
   */
  public static double[] normalizeSamples(double[] samples_to_normalize)
  {
    double[] normalized_samples = new double[samples_to_normalize.length];
    for (int samp = 0; samp < normalized_samples.length; samp++)
      normalized_samples[samp] = samples_to_normalize[samp];

    double max_sample_value = 0.0;
    for (int samp = 0; samp < normalized_samples.length; samp++)
      if (Math.abs(normalized_samples[samp]) > max_sample_value)
        max_sample_value = Math.abs(normalized_samples[samp]);
    if (max_sample_value != 0.0)
      for (int samp = 0; samp < normalized_samples.length; samp++)
        normalized_samples[samp] /= max_sample_value;

    return normalized_samples;
  }
}
