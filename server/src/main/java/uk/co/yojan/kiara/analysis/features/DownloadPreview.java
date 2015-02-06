package uk.co.yojan.kiara.analysis.features;

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
//
//  private static boolean normalise = true;
//
//  public static double[] download(String id) throws Exception {
//
//    Api api = SpotifyApi.clientCredentialsApi();
//
//    TrackRequest request = api.getTrack(id).build();
//    Track track = request.get();
//    URLConnection conn = new URL(track.getPreviewUrl()).openConnection();
//    InputStream is = conn.getInputStream();
//
//    short[] buff = decodeMp3(is, null);
//
//    double[] samples = null;
//    double[][] channel_samples = extractSampleValues(buff, 2, 16);
//
//
//
//    if (channel_samples.length > 1)
//      samples = getSamplesMixedDownIntoOneChannel(channel_samples);
//    else if(channel_samples.length == 1)
//      samples = channel_samples[0];
//    else if(channel_samples.length < 1)
//      samples = null;
//
//    if (normalise)
//      samples = normalizeSamples(samples);
//
//    return samples;
//  }
//
//  protected static int countFrames(InputStream in)
//  {
//    return -1;
//  }
//
//  public static short[] decodeMp3(InputStream sourceStream, Decoder.Params decoderParams) throws IOException {
//    try {
//      if (!(sourceStream instanceof BufferedInputStream))
//        sourceStream = new BufferedInputStream(sourceStream);
//      int frameCount = -1;
//      if (sourceStream.markSupported()) {
//        sourceStream.mark(-1);
//        frameCount = countFrames(sourceStream);
//        sourceStream.reset();
//      }
//
//
//      ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//      SampleBuffer output = null;
//      Decoder decoder = new Decoder(decoderParams);
//      Bitstream stream = new Bitstream(sourceStream);
//
//      if (frameCount==-1)
//        frameCount = Integer.MAX_VALUE;
//
//      int frame = 0;
//
//      try
//      {
//        for (; frame<frameCount; frame++)
//        {
//          try
//          {
//            Header header = stream.readFrame();
//            if (header==null)
//              break;
//
//            if (output==null)
//            {
//              // REVIEW: Incorrect functionality.
//              // the decoder should provide decoded
//              // frequency and channels output as it may differ from
//              // the source (e.g. when downmixing stereo to mono.)
//              int channels = (header.mode()==Header.SINGLE_CHANNEL) ? 1 : 2;
//              int freq = header.frequency();
//              output = new SampleBuffer(freq, channels);
//              decoder.setOutputBuffer(output);
//            }
//
//            Obuffer decoderOutput = decoder.decodeFrame(header, stream);
//            short[] shBu = output.getBuffer();
//            ByteBuffer bb = ByteBuffer.allocate(shBu.length * 2);
//            for(short s : shBu) bb.putShort(s);
//            outputStream.write(bb.array());
//
//
//            // REVIEW: the way the output buffer is set
//            // on the decoder is a bit dodgy. Even though
//            // this exception should never happen, we test to be sure.
//            if (decoderOutput!=output)
//              throw new InternalError("Output buffers are different.");
//
//            stream.closeFrame();
//
//          } catch (BitstreamException e) {
//            e.printStackTrace();
//          } catch (DecoderException e) {
//            e.printStackTrace();
//          }
//        }
//
//      } catch(Exception e) {
//        e.printStackTrace();
//      }
//      finally
//      {
//
//        if (output!=null) {
//          return convert(outputStream.toByteArray());
//        } else {
//          return null;
//        }
//      }
//    }
//    catch (IOException ex)
//    {
//      ex.printStackTrace();
//    }
//    return null;
//  }
//
//  private static short[] convert(byte[] buffer) {
//    int size = buffer.length;
//    short[] shortArray = new short[size];
//
//    for (int i = 0; i < size - 1; i += 2) {
//      byte high = buffer[i];
//      byte low = buffer[i + 1];
//      shortArray[i] = (short) (((high & 0xFF) << 8) | (low & 0xFF));
//    }
//    return shortArray;
//  }
//
//  private static double[][] extractSampleValues(short[] buffer, int channels, int bit_depth) {
//
//    ShortBuffer shortBuffer = ShortBuffer.wrap(buffer); //l..asShortBuffer();
//
//    // Find the maximum possible value that a sample may have with the given
//    // bit depth
//    double max_sample_value = findMaximumSampleValue(bit_depth) + 2.0;
//
//    // number of bytes / sample size / channels
//    int number_samples = buffer.length / channels;
//    double[][] samples = new double[channels][number_samples];
//
//    for (int samp = 0; samp < number_samples; samp++)
//      for (int chan = 0; chan < channels; chan++)
//        samples[chan][samp] = (double) shortBuffer.get() / max_sample_value;
//
//    return samples;
//  }
//
//  /**
//   * Returns the maximum possible value that a signed sample can have under
//   * the given bit depth. May be 1 or 2 values smaller than actual max,
//   * depending on specifics of encoding used.
//   *
//   * @param	bit_depth	The bit depth to examine.
//   * @return				The maximum possible positive sample value as a double.
//   */
//  public static double findMaximumSampleValue(int bit_depth) {
//    int max_sample_value_int = 1;
//    for (int i = 0; i < (bit_depth - 1); i++)
//      max_sample_value_int *= 2;
//    max_sample_value_int--;
//    double max_sample_value = ((double) max_sample_value_int) - 1.0;
//    return max_sample_value;
//  }
//
//  /* DSPMethods ****/
//
//  /**
//   * Returns the given set of samples as a set of samples mixed down into one
//   * channel.
//   *
//   * @param	audio_samples	Audio samles to modify, with a minimum value of
//   *							-1 and a maximum value of +1. The first indice
//   *							corresponds to the channel and the second indice
//   *							corresponds to the sample number.
//   * @return					The given audio samples mixed down, with equal
//   *							gain, into one channel.
//   */
//  public static double[] getSamplesMixedDownIntoOneChannel(double[][] audio_samples)
//  {
//    if (audio_samples.length == 1)
//      return audio_samples[0];
//
//    double number_channels = (double) audio_samples.length;
//    int number_samples = audio_samples[0].length;
//
//    double[] samples_mixed_down = new double[number_samples];
//    for (int samp = 0; samp < number_samples; samp++)
//    {
//      double total_so_far = 0.0;
//      for (int chan = 0; chan < number_channels; chan++)
//        total_so_far += audio_samples[chan][samp];
//      samples_mixed_down[samp] = total_so_far / number_channels;
//    }
//
//    return samples_mixed_down;
//  }
//
//  /**
//   * Normalizes the given samples so that the absolute value of the highest
//   * sample amplitude is 1. Does nothing if also samples are 0.
//   *
//   * @param	samples_to_normalize	The samples to normalize.
//   * @return							Returns a copy of the given samples
//   *									after normalization.
//   */
//  public static double[] normalizeSamples(double[] samples_to_normalize)
//  {
//    double[] normalized_samples = new double[samples_to_normalize.length];
//    for (int samp = 0; samp < normalized_samples.length; samp++)
//      normalized_samples[samp] = samples_to_normalize[samp];
//
//    double max_sample_value = 0.0;
//    for (int samp = 0; samp < normalized_samples.length; samp++)
//      if (Math.abs(normalized_samples[samp]) > max_sample_value)
//        max_sample_value = Math.abs(normalized_samples[samp]);
//    if (max_sample_value != 0.0)
//      for (int samp = 0; samp < normalized_samples.length; samp++)
//        normalized_samples[samp] /= max_sample_value;
//
//    return normalized_samples;
//  }
//
//  public static void main(String[] args) throws Exception {
//    double[] samples;
//    try {
//      samples = DownloadPreview.download("7xv1f3W3rhNMEQKc0YLthi");
//      MFCCAreaMoment mfcc = new MFCCAreaMoment(samples);
//      ArrayList<Double> averages = mfcc.averageMoments();
//      System.out.print("");
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
//
//  }
}
