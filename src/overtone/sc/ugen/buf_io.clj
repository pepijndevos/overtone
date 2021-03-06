(ns overtone.sc.ugen.buf-io
  (:use (overtone.sc.ugen common)))

(def specs
     [
      {:name "PlayBuf",
       :args [{:name "numChannels" :mode :num-outs}
              {:name "bufnum", :default 0}
              {:name "rate", :default 1.0}
              {:name "trigger", :default 1.0}
              {:name "startPos", :default 0.0}
              {:name "loop", :default 0.0}
              {:name "action", :default :none :map DONE-ACTIONS}]
       :doc "play samples from a buffer"}

      ;; TGrains : MultiOutUGen {
      ;; 	*ar { arg numChannels, trigger=0, bufnum=0, rate=1, centerPos=0,
      ;; 			dur=0.1, pan=0, amp=0.1, interp=4;
      ;; 		if (numChannels < 2) {
      ;; 			 "TGrains needs at least two channels.".error;
      ;; 			 ^nil
      ;; 		}
      ;; 		^this.multiNew('audio', numChannels, trigger, bufnum, rate, centerPos,
      ;; 				dur, pan, amp, interp)
      ;; 	}
      ;; 	init { arg argNumChannels ... theInputs;
      ;; 		inputs = theInputs;
      ;; 		^this.initOutputs(argNumChannels, rate);
      ;; 	}
      ;; 	argNamesInputsOffset { ^2 }
      ;; }

      {:name "TGrains",
       :args [{:name "numChannels" :mode :num-outs :default 2}
              {:name "trigger", :default 0}
              {:name "bufnum", :default 0}
              {:name "rate", :default 1}
              {:name "centerPos", :default 0}
              {:name "dur", :default 0.1}
              {:name "pan", :default 0.0}
              {:name "amp", :default 0.1}
              {:name "interp", :default 4}],
       :rates #{:ar}
       :check (num-outs-greater-than 1)
       :doc "sample playback from a buffer with fine control for doing granular synthesis"}

      {:name "BufRd",
       :args [{:name "numChannels"    :default 1, :mode :num-outs}
              {:name "bufnum",        :default 0}
              {:name "phase",         :default 0.0}
              {:name "loop",          :default 1.0}
              {:name "interpolation", :default 2}]
       :check (when-ar (nth-input-ar 1))   ; check phase. NB numChannels has already been popped.
       :doc "reads the contents of a buffer at a given index"}

      {:name "BufWr",
       :args [{:name "inputArray", :mode :append-sequence}
              {:name "bufnum", :default 0}
              {:name "phase", :default 0.0}
              {:name "loop", :default 1.0}]
       :check (when-ar (nth-input-ar 1))
       :doc "writes to a buffer at a given index"}

      {:name "RecordBuf",
       :args [{:name "inputArray", :mode :append-sequence}
              {:name "bufnum", :default 0}
              {:name "offset", :default 0.0}
              {:name "recLevel", :default 1.0}
              {:name "preLevel", :default 0.0}
              {:name "run", :default 1.0}
              {:name "loop", :default 1.0}
              {:name "trigger", :default 1.0}
              {:name "action", :default 0 :map DONE-ACTIONS}]
       :doc "record a stream of values into a buffer"}

      {:name "ScopeOut",
       :args [{:name "inputArray", :mode :append-sequence}
              {:name "bufnum", :default 0.0}],
       :num-outs 0}

      ;; TODO investigate local bufs system, flesh out LocalBuf

      ;; LocalBuf : UGen {
      ;; 	*new { arg numFrames = 1, numChannels = 1;
      ;; 		^this.multiNew('scalar', numChannels, numFrames)
      ;; 	}
      ;;
      ;; 	*new1 { arg rate ... args;
      ;; 		var maxLocalBufs = UGen.buildSynthDef.maxLocalBufs;
      ;; 		if(maxLocalBufs.isNil) {
      ;; 			maxLocalBufs = MaxLocalBufs.new;
      ;; 			UGen.buildSynthDef.maxLocalBufs = maxLocalBufs;
      ;; 		};
      ;; 		maxLocalBufs.increment;
      ;; 		^super.new.rate_(rate).addToSynth.init( *args ++ maxLocalBufs )
      ;; 	}
      ;;
      ;; 	*newFrom { arg list;
      ;; 		var shape, buf;
      ;; 		shape = list.shape;
      ;; 		if(shape.size == 1) { shape = [1, list.size] };
      ;; 		if(shape.size > 2) { Error("LocalBuf: list has not the right shape").throw };
      ;; 		buf = this.new(*shape.reverse);
      ;; 		buf.set(list.flop.flat);
      ;; 		^buf
      ;; 	}
      ;;
      ;; 	numFrames { ^inputs[1] }
      ;; 	numChannels { ^inputs[0] }
      ;;
      ;; 	set { arg values, offset = 0;
      ;; 		SetBuf(this, values.asArray, offset);
      ;; 	}
      ;; 	clear {
      ;; 		ClearBuf(this);
      ;; 	}
      ;; }

      {:name "LocalBuf"
       :args [{:name "numFrames" :default 1}
              {:name "numChannels" :mode :num-outs :default 1}]}

      ;; MaxLocalBufs : UGen {
      ;; 	*new {
      ;; 		^this.multiNew('scalar', 0);
      ;; 	}
      ;; 	increment {
      ;; 		inputs[0] = inputs[0] + 1;
      ;; 	}
      ;; }

      ;; TODO increment method, what does it do?
      {:name "MaxLocalBufs"
       :args []
       :rates #{:ir}}

      ;; SetBuf : UGen {
      ;; 	*new { arg buf, values, offset = 0;
      ;; 		^this.multiNewList(['scalar', buf, offset, values.size] ++ values)
      ;; 	}
      ;; }

      {:name "SetBuf"
       :args [{:name "buf"}
              {:name "values" :mode :not-expanded}
              {:name "offset" :default 0}]
       :init (fn [rate [buf values offset] spec]
               (apply vector buf offset (count values) values))}

      ;; ClearBuf : UGen {
      ;; 	*new { arg buf;
      ;; 		^this.multiNew('scalar', buf)
      ;; 	}
      ;; }

      {:name "ClearBuf",
       :args [{:name "buf"}],
       :rates #{:ir}}])
