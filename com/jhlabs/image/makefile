# Java Image Processing code from jhlabs.
#
# @Version: $Id$
# @Author: Christopher Brooks (makefile only)
#
# @Copyright (c) 2014 The Regents of the University of California.
# All rights reserved.
#
# Permission is hereby granted, without written agreement and without
# license or royalty fees, to use, copy, modify, and distribute this
# software and its documentation for any purpose, provided that the
# above copyright notice and the following two paragraphs appear in all
# copies of this software.
#
# IN NO EVENT SHALL THE UNIVERSITY OF CALIFORNIA BE LIABLE TO ANY PARTY
# FOR DIRECT, INDIRECT, SPECIAL, INCIDENTAL, OR CONSEQUENTIAL DAMAGES
# ARISING OUT OF THE USE OF THIS SOFTWARE AND ITS DOCUMENTATION, EVEN IF
# THE UNIVERSITY OF CALIFORNIA HAS BEEN ADVISED OF THE POSSIBILITY OF
# SUCH DAMAGE.
#
# THE UNIVERSITY OF CALIFORNIA SPECIFICALLY DISCLAIMS ANY WARRANTIES,
# INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
# MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE SOFTWARE
# PROVIDED HEREUNDER IS ON AN "AS IS" BASIS, AND THE UNIVERSITY OF
# CALIFORNIA HAS NO OBLIGATION TO PROVIDE MAINTENANCE, SUPPORT, UPDATES,
# ENHANCEMENTS, OR MODIFICATIONS.
#
# 						PT_COPYRIGHT_VERSION_2
# 						COPYRIGHTENDKEY
##########################################################################

ME = 		com/jhlabs/image

DIRS =		

# Root of Ptolemy II directory
ROOT =		../../..

CLASSPATH =	$(ROOT)$(CLASSPATHSEPARATOR)$(PTWEBCAMCAPTURE_JARS)

# Get configuration info
CONFIG =	$(ROOT)/mk/ptII.mk
include $(CONFIG)

# Used to build jar files
PTPACKAGE = 	image
PTCLASSJAR = 	$(PTPACKAGE).jar

JSRCS = \
	AbstractBufferedImageOp.java \
	ApplyMaskFilter.java \
	ArrayColormap.java \
	AverageFilter.java \
	BicubicScaleFilter.java \
	BinaryFilter.java \
	BlockFilter.java \
	BlurFilter.java \
	BorderFilter.java \
	BoxBlurFilter.java \
	BrushedMetalFilter.java \
	BumpFilter.java \
	CausticsFilter.java \
	CellularFilter.java \
	ChannelMixFilter.java \
	CheckFilter.java \
	ChromaKeyFilter.java \
	ChromeFilter.java \
	CircleFilter.java \
	ColorHalftoneFilter.java \
	Colormap.java \
	CompositeFilter.java \
	CompoundFilter.java \
	ContourFilter.java \
	ContrastFilter.java \
	ConvolveFilter.java \
	CropFilter.java \
	CrystallizeFilter.java \
	CurlFilter.java \
	Curve.java \
	CurvesFilter.java \
	DeinterlaceFilter.java \
	DespeckleFilter.java \
	DiffuseFilter.java \
	DiffusionFilter.java \
	DilateFilter.java \
	DisplaceFilter.java \
	DissolveFilter.java \
	DitherFilter.java \
	DoGFilter.java \
	EdgeFilter.java \
	EmbossFilter.java \
	EqualizeFilter.java \
	ErodeAlphaFilter.java \
	ErodeFilter.java \
	ExposureFilter.java \
	FBMFilter.java \
	FadeFilter.java \
	FeedbackFilter.java \
	FieldWarpFilter.java \
	FillFilter.java \
	FlareFilter.java \
	FlipFilter.java \
	Flush3DFilter.java \
	FourColorFilter.java \
	GainFilter.java \
	GammaFilter.java \
	GaussianFilter.java \
	GlintFilter.java \
	GlowFilter.java \
	Gradient.java \
	GradientFilter.java \
	GradientWipeFilter.java \
	GrayFilter.java \
	GrayscaleColormap.java \
	GrayscaleFilter.java \
	HSBAdjustFilter.java \
	HalftoneFilter.java \
	HighPassFilter.java \
	Histogram.java \
	ImageCombiningFilter.java \
	ImageMath.java \
	ImageUtils.java \
	InterpolateFilter.java \
	InvertAlphaFilter.java \
	InvertFilter.java \
	IteratedFilter.java \
	JavaLnFFilter.java \
	KaleidoscopeFilter.java \
	KeyFilter.java \
	LaplaceFilter.java \
	LensBlurFilter.java \
	LevelsFilter.java \
	LifeFilter.java \
	LightFilter.java \
	LinearColormap.java \
	LookupFilter.java \
	MapColorsFilter.java \
	MapFilter.java \
	MarbleFilter.java \
	MarbleTexFilter.java \
	MaskFilter.java \
	MaximumFilter.java \
	MedianFilter.java \
	MinimumFilter.java \
	MirrorFilter.java \
	MotionBlurFilter.java \
	MotionBlurOp.java \
	MotionDetectorFilter.java \
	MutatableFilter.java \
	NoiseFilter.java \
	OctTreeQuantizer.java \
	OffsetFilter.java \
	OilFilter.java \
	OpacityFilter.java \
	OutlineFilter.java \
	PerspectiveFilter.java \
	PinchFilter.java \
	PixelUtils.java \
	PlasmaFilter.java \
	PointFilter.java \
	PointillizeFilter.java \
	PolarFilter.java \
	PosterizeFilter.java \
	PremultiplyFilter.java \
	QuantizeFilter.java \
	Quantizer.java \
	QuiltFilter.java \
	RGBAdjustFilter.java \
	RaysFilter.java \
	ReduceNoiseFilter.java \
	RenderTextFilter.java \
	RescaleFilter.java \
	RippleFilter.java \
	RotateFilter.java \
	SaturationFilter.java \
	ScaleFilter.java \
	ScratchFilter.java \
	ShadeFilter.java \
	ShadowFilter.java \
	ShapeFilter.java \
	SharpenFilter.java \
	ShatterFilter.java \
	ShearFilter.java \
	ShineFilter.java \
	SkeletonFilter.java \
	SkyFilter.java \
	SmartBlurFilter.java \
	SmearFilter.java \
	SolarizeFilter.java \
	SparkleFilter.java \
	Spectrum.java \
	SpectrumColormap.java \
	SphereFilter.java \
	SplineColormap.java \
	StampFilter.java \
	SwimFilter.java \
	SwizzleFilter.java \
	TextureFilter.java \
	ThresholdFilter.java \
	TileImageFilter.java \
	TransferFilter.java \
	TransformFilter.java \
	TransitionFilter.java \
	TritoneFilter.java \
	TwirlFilter.java \
	UnpremultiplyFilter.java \
	UnsharpFilter.java \
	VariableBlurFilter.java \
	WarpFilter.java \
	WarpGrid.java \
	WaterFilter.java \
	WeaveFilter.java \
	WholeImageFilter.java \
	WoodFilter.java

OTHER_FILES_TO_BE_JARED =  

EXTRA_SRCS =	$(JSRCS) $(OTHER_FILES_TO_BE_JARED)


# Sources that may or may not be present, but if they are present, we don't
# want make checkjunk to barf on them.
# Don't include $(DIRS) here, we don't want make sources to run make demo
MISC_FILES =	#$(DIRS)

# make checkjunk will not report OPTIONAL_FILES as trash
# make distclean removes OPTIONAL_FILES
# driver is in OPTIONAL_FILES because we don't ship it.
OPTIONAL_FILES = \
	'CellularFilter$$Point.class' \
	'ChromeFilter$$1.class' \
	'CurlFilter$$Sampler.class' \
	'FieldWarpFilter$$Line.class' \
	'LightFilter$$1.class' \
	'LightFilter$$AmbientLight.class' \
	'LightFilter$$DistantLight.class' \
	'LightFilter$$Light.class' \
	'LightFilter$$Material.class' \
	'LightFilter$$PointLight.class' \
	'LightFilter$$SpotLight.class' \
	'OctTreeQuantizer$$OctTreeNode.class' \
	'ShatterFilter$$Tile.class'

JCLASS = $(JSRCS:%.java=%.class)

all: jclass
install: jclass $(PTCLASSJAR)
docs: javadocs

# Include the rest of the rules
include $(ROOT)/mk/ptcommon.mk

