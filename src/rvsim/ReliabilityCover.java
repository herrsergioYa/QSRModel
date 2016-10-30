/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package rvsim;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import rnsim.Node;
import static rnsim.ReliabilityNetwork.extractOks;
import util.IdentityHashSet;

/**
 *
 * @author HerrSergio
 */
public class ReliabilityCover {
    public static Set<Node> areOk(Map<Node, ? extends Collection<Node>> matrix,
            Collection<Node> sources,
            Collection<Node> dests
            ) {
        Set<Node> result = new IdentityHashSet<>();
        Collection<Node> nodes = extractOks(sources);
        ArrayDeque<Node> points = new ArrayDeque<>(nodes);
        IdentityHashSet<Node> checked = new IdentityHashSet<>(nodes);
        while(points.size() > 0) {
            Node node = points.poll();
            if(dests.contains(node)) {
                result.add(node);
            }
            if(matrix.containsKey(node)) {
                nodes = extractOks(matrix.get(node));
                nodes.removeAll(checked);
                points.addAll(nodes);
                checked.addAll(nodes);
            }            
        }
        return result;
    }
    
    public static List<String> getNames(Collection<? extends Node> nodes) {
        return nodes.stream().map(nd -> nd.getCh().getName()).distinct().collect(Collectors.toList());//toArray(String[]::new);
    }
}
