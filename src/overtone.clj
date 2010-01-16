(ns overtone
  (:require config time-utils log midi osc byte-spec
     (overtone.core sc ugen synth synthdef envelope)
     (overtone.music rhythm pitch tuning)))

; TODO: make this work with namespace prefixes too... 
;   (immigrate 'overtone.instruments)
(defn immigrate
 "Create a public var in this namespace for each public var in the
 namespaces named by ns-names. The created vars have the same name, value,
 and metadata as the original except that their :ns metadata value is this
 namespace."
 [& ns-names]
 (doseq [ns ns-names]
   (require ns)
   (doseq [[sym var] (ns-publics ns)]
     (let [sym (with-meta sym (assoc (meta var) :ns *ns*))]
       (if (.isBound var)
         (intern *ns* sym (var-get var))
         (intern *ns* sym))))))

(immigrate
  'osc
  'midi
  'time-utils
  'overtone.core.util
  'overtone.core.sc
  'overtone.core.ugen
  'overtone.core.envelope
  'overtone.core.synth
  'overtone.core.synthdef
  'overtone.music.rhythm
  'overtone.music.pitch
  'overtone.music.tuning
  )

;(refer-ugens *ns*)
