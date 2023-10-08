import 'dart:math';
import 'dart:ui';

import 'package:dual_screen/dual_screen.dart';
import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';

void main() {
  runApp(const MyApp());
}

class MyApp extends StatelessWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Flutter Demo',
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: Colors.deepPurple),
        useMaterial3: true,
      ),
      home: const FoldingFeatureExample(title: 'Folding Feature Example'),
    );
  }
}

class FoldingFeatureExample extends StatefulWidget {
  const FoldingFeatureExample({super.key, required this.title});

  final String title;

  @override
  State<FoldingFeatureExample> createState() => _FoldingFeatureExampleState();
}

class _FoldingFeatureExampleState extends State<FoldingFeatureExample> {
  @override
  Widget build(BuildContext context) {
    final displayFeatures = MediaQuery.of(context).displayFeatures;
    if (kDebugMode) {
      for (final feature in displayFeatures) {
        print('feature: $feature');
      }
    }

    final foldingFeatures = displayFeatures.where((feature) {
      return feature.type == DisplayFeatureType.fold ||
          feature.type == DisplayFeatureType.hinge;
    }).toList();

    final screenSize = MediaQuery.of(context).size;

    return Stack(
      children: [
        TwoPane(
          startPane: Scaffold(
            appBar: AppBar(
              backgroundColor: Theme.of(context).colorScheme.inversePrimary,
              title: Text(widget.title),
            ),
            body: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: foldingFeatures.isNotEmpty
                  ? <Widget>[
                      Text(
                        'bounds: ${foldingFeatures.first.bounds}',
                        style: const TextStyle(fontSize: 21),
                      ),
                      Text(
                        'type: ${foldingFeatures.first.type}',
                        style: const TextStyle(fontSize: 21),
                      ),
                      Text(
                        'state: ${foldingFeatures.first.state}',
                        style: const TextStyle(fontSize: 21),
                      ),
                    ]
                  : <Widget>[
                      const Text('No folding features found'),
                    ],
            ),
          ),
          endPane: Material(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                FutureBuilder<bool>(
                  future: DualScreenInfo.hasHingeAngleSensor,
                  builder: (conext, hasHingeAngleSensor) {
                    return Text(
                      'hasHingeAngleSensor: ${hasHingeAngleSensor.data}',
                      style: const TextStyle(fontSize: 21),
                    );
                  },
                ),
                StreamBuilder<double>(
                  stream: DualScreenInfo.hingeAngleEvents,
                  builder: (context, hingeAngle) {
                    return Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'hingeAngle: ${hingeAngle.data}',
                          style: const TextStyle(fontSize: 21),
                        ),
                        Align(
                          child: CustomPaint(
                            size: Size(screenSize.width, 10),
                            painter: ArcPainter(angle: hingeAngle.data ?? 0.0),
                          ),
                        ),
                      ],
                    );
                  },
                ),
              ],
            ),
          ),
          paneProportion: 0.3,
          panePriority: screenSize.width > 500
              ? TwoPanePriority.both
              : TwoPanePriority.start,
        ),
        if (foldingFeatures.isNotEmpty)
          CustomPaint(
            size: Size.infinite,
            painter:
                _DisplayFeaturePainter(bounds: foldingFeatures.first.bounds),
          )
        else
          const SizedBox(),
      ],
    );
  }
}

class _DisplayFeaturePainter extends CustomPainter {
  const _DisplayFeaturePainter({required this.bounds});
  final Rect bounds;

  @override
  void paint(Canvas canvas, Size size) {
    final rect = bounds;
    final paint = Paint()
      ..color = Colors.blue
      ..strokeWidth = 2.0
      ..style = PaintingStyle.stroke;

    canvas.drawRect(rect, paint);
  }

  @override
  bool shouldRepaint(CustomPainter oldDelegate) {
    return false;
  }
}

class ArcPainter extends CustomPainter {
  ArcPainter({required this.angle});
  final double angle;

  @override
  void paint(Canvas canvas, Size size) {
    final rect = Rect.fromCircle(
      center: Offset(size.width / 2, size.height),
      radius: size.width / 2,
    );

    const startAngle = 0.0;
    final sweepAngle = (angle / 360) * 2 * pi;

    final paint = Paint()
      ..color = Colors.blue
      ..style = PaintingStyle.fill;

    canvas.drawArc(rect, startAngle, sweepAngle, true, paint);
  }

  @override
  bool shouldRepaint(covariant CustomPainter oldDelegate) {
    return true;
  }
}
