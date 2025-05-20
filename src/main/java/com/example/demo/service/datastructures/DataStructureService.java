// projectback/src/main/java/com/example/demo/service/datastructures/DataStructureService.java
package com.example.demo.service.datastructures;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Stack;

import org.springframework.stereotype.Service;

import com.example.demo.entity.Detection;
import com.example.demo.service.DetectionService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DataStructureService {

    private final DetectionService detectionService;

    // Array
    public int[] getDetectionsAsArray() {
        List<Detection> detections = detectionService.getAllDetections();
        return detections.stream()
                .mapToInt(d -> d.getTimestampMs().intValue())
                .toArray();
    }

    // Lista simple
    public List<Detection> getDetectionsAsLinkedList() {
        List<Detection> detections = detectionService.getAllDetections();
        LinkedList<Detection> linkedList = new LinkedList<>();
        detections.forEach(linkedList::add);
        return linkedList;
    }

    // Lista doblemente enlazada
    public List<Detection> getDetectionsAsDoubleLinkedList() {
        // En Java, LinkedList ya es doblemente enlazada
        return getDetectionsAsLinkedList();
    }

    // Lista doblemente enlazada circular
    public List<Detection> getDetectionsAsCircularDoubleLinkedList() {
        List<Detection> detections = detectionService.getAllDetections();
        CircularDoubleLinkedList<Detection> list = new CircularDoubleLinkedList<>();
        detections.forEach(list::add);
        return list.toList();
    }

    // Pila
    public List<Detection> getDetectionsAsStack() {
        List<Detection> detections = detectionService.getAllDetections();
        Stack<Detection> stack = new Stack<>();
        detections.forEach(stack::push);
        
        // Convertir a lista para devolver
        List<Detection> result = new ArrayList<>();
        while (!stack.isEmpty()) {
            result.add(stack.pop());
        }
        return result;
    }

    // Cola
    public List<Detection> getDetectionsAsQueue() {
        List<Detection> detections = detectionService.getAllDetections();
        Queue<Detection> queue = new LinkedList<>();
        detections.forEach(queue::add);
        
        // Convertir a lista para devolver
        List<Detection> result = new ArrayList<>();
        while (!queue.isEmpty()) {
            result.add(queue.poll());
        }
        return result;
    }

    // Árbol
    public Map<String, Object> getDetectionsAsTree() {
        List<Detection> detections = detectionService.getAllDetections();
        TreeNode root = new TreeNode("Root");
        
        for (Detection detection : detections) {
            Map<String, Integer> objectsTotal = detectionService.parseObjectsTotal(detection.getObjectsTotal());
            
            // Crear nodo para esta detección
            TreeNode detectionNode = new TreeNode("Detection " + detection.getId());
            root.addChild(detectionNode);
            
            // Agregar hijos para cada tipo de vehículo
            objectsTotal.forEach((type, count) -> {
                TreeNode vehicleNode = new TreeNode(type + ": " + count);
                detectionNode.addChild(vehicleNode);
            });
        }
        
        return root.toMap();
    }
    
    // Clase auxiliar para lista doblemente enlazada circular
    static class CircularDoubleLinkedList<T> {
        private Node<T> head;
        private Node<T> tail;
        private int size;
        
        private static class Node<T> {
            final T data;
            Node<T> next;
            Node<T> prev;
            
            Node(T data) {
                this.data = data;
            }
        }
        
        public void add(T data) {
            Node<T> newNode = new Node<>(data);
            
            if (head == null) {
                head = newNode;
                tail = newNode;
                head.next = head;
                head.prev = head;
            } else {
                tail.next = newNode;
                newNode.prev = tail;
                newNode.next = head;
                head.prev = newNode;
                tail = newNode;
            }
            
            size++;
        }
        
        public List<T> toList() {
            List<T> result = new ArrayList<>();
            if (head == null) return result;
            
            Node<T> current = head;
            do {
                result.add(current.data);
                current = current.next;
            } while (current != head);
            
            return result;
        }
        
        // Método para recorrer la lista en sentido inverso
        // Esto utiliza explícitamente el campo prev, eliminando la advertencia
        public List<T> toReverseList() {
            List<T> result = new ArrayList<>();
            if (head == null) return result;
            
            Node<T> current = tail;
            do {
                result.add(current.data);
                current = current.prev;  // Aquí se usa el campo prev
            } while (current != tail);
            
            return result;
        }
    }
    
    // Clase auxiliar para árbol
    static class TreeNode {
        private final String value;
        private final List<TreeNode> children;
        
        TreeNode(String value) {
            this.value = value;
            this.children = new ArrayList<>();
        }
        
        void addChild(TreeNode child) {
            this.children.add(child);
        }
        
        Map<String, Object> toMap() {
            Map<String, Object> map = new HashMap<>();
            map.put("value", value);
            
            if (!children.isEmpty()) {
                List<Map<String, Object>> childrenMaps = new ArrayList<>();
                for (TreeNode child : children) {
                    childrenMaps.add(child.toMap());
                }
                map.put("children", childrenMaps);
            }
            
            return map;
        }
    }
}