import edu.princeton.cs.algs4.Point2D;
import edu.princeton.cs.algs4.RectHV;

import java.util.ArrayList;
import java.util.List;

public class KdTree {

    private class Node {
        public Point2D p;
        public RectHV rect;
        public Node left;
        public Node right;
        public boolean isSplitX;

        public Node(Point2D p, RectHV rect, boolean isSplitX) {
            this.p = p;
            this.rect = rect;
            this.isSplitX = isSplitX;
        }
    }

    private Node root;
    private int size;

    public KdTree() {
        root = null;
        size = 0;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }

    public void draw() {


    }

    private Node insert(Node node, Point2D p, boolean isSplitX,
                       double xMin,
                       double yMin,
                       double xMax,
                       double yMax) {
        if (node == null) {
            RectHV newRect = new RectHV(xMin, yMin, xMax, yMax);
            return new Node(p, newRect, isSplitX);
        }

        boolean newIsSplitX = !isSplitX;
        if (node.isSplitX) {
            if (p.x() < node.p.x()) {
                node.left = insert(node.left, p, newIsSplitX, xMin, yMin, node.p.x(), yMax);
            } else {
                node.right = insert(node.right, p, newIsSplitX, node.p.x(), yMin, xMax, yMax);
            }
        } else {
            if (p.y() < node.p.y()) {
                node.left = insert(node.left, p, newIsSplitX, xMin, yMin, xMax, node.p.y());
            } else {
                node.right = insert(node.right, p, newIsSplitX, xMin, node.p.y(), xMax, yMax);
            }
        }
        return node;
    }

    public void insert(Point2D p) {

        if (p == null) {
            throw new IllegalArgumentException();
        }

        if (contains(p)) {
            return;
        }

        size++;

        root = insert(root, p, true, 0, 0, 1, 1);

    }

    public boolean contains(Point2D p) {

        if (p == null) {
            throw new IllegalArgumentException();
        }

        Node node = root;

        while (node != null) {
            if (node.p.equals(p)) {
                return true;
            }
            node = getNode(p, node);
        }
        return false;
    }

    private Node getNode(Point2D p, Node node) {
        if (node.isSplitX) {
            if (p.x() < node.p.x()) {
                node = node.left;
            } else {
                node = node.right;
            }
        } else {
            if (p.y() < node.p.y()) {
                node = node.left;
            } else {
                node = node.right;
            }
        }
        return node;
    }

    private void range(Node node, RectHV rect, List<Point2D> list) {
        if (node == null || !rect.intersects(node.rect)) {
            return;
        }
        if (rect.contains(node.p)) {
            list.add(node.p);
        }
        range(node.left, rect, list);
        range(node.right, rect, list);
    }

    public Iterable<Point2D> range(RectHV rect) {
        if (rect == null) {
            throw new IllegalArgumentException();
        }
        List<Point2D> list = new ArrayList<>();
        range(root, rect, list);
        return list;
    }

    private Point2D nearest(Node node, Point2D p, Point2D bestPoint) {
        if (node == null) {
            return bestPoint;
        }
        double bestDist = p.distanceSquaredTo(bestPoint);
        if (node.rect.distanceSquaredTo(p) > bestDist) {
            return bestPoint;
        }
        double dist = p.distanceSquaredTo(node.p);
        if (dist < bestDist) {
            bestPoint = node.p;
        }

        Node first, second;

        if (node.isSplitX) {
            if (p.x() < node.p.x()) {
                first = node.left;
                second = node.right;
            } else {
                first = node.right;
                second = node.left;
            }
        } else {
            if (p.y() < node.p.y()) {
                first = node.left;
                second = node.right;
            } else {
                first = node.right;
                second = node.left;
            }
        }
        bestPoint = nearest(first, p, bestPoint);
        bestDist = p.distanceSquaredTo(bestPoint);
        if (second != null && second.rect.distanceSquaredTo(p) < bestDist) {
            bestPoint = nearest(second, p, bestPoint);
        }

        return bestPoint;
    }

    public Point2D nearest(Point2D p) {
        if (p == null) {
            throw new IllegalArgumentException();
        }
        if (root == null) {
            return null;
        }
        return nearest(root, p, root.p);
    }
}
