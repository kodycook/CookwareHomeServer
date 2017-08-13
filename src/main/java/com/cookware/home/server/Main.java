package com.cookware.home.server;

import java.io.File;

/**
 * Created by Kody on 13/08/2017.
 */
public class Main {

    public static void main( String[] args ) {
        System.out.println("Hello World!");

        WebVideoDownloader primewireDownloader = new WebVideoDownloader();
        primewireDownloader.newDownload(DownloadType.TV, "https://n3125.thevideo.me:8777/6gjtauqfh6oammfvg6mvktetpmqxa4py45x5pj67f3o5pa2dgblzcfq5paavn3ixgbtme3n5kp4z53vu7ohtdgibkjs56d3d7oexhvzik6y6epc56jfvheotgct6lcjakjcovotatwchfg5ke73btwoghf4zk4skvuzl4cfiijqlswkchgndz5esm7hjsetvril7juw7l7auonx7md6luptgefjw7ewrbzzzkptywebjvlwepwb6yd4uiqlnfkaqmtwodpldcjgeqg43l733433prbnq/v.mp4?direct=false&ua=1&vt=gg4p3ci635fhox3oewfmaiovz5yfzdvsekvzbdrfloohxcpfb64ulqfjxsgli2iox2rpldcdeustag7ddx7iclbh2esepzjwdr6yd6jcekqd6oyfqx5uxwrlceegbdslmpeghsrmh2d2kmpyabqeforjs4sjqx7lm5mcy443kktamtbf7qdg5zwo3cxmiw7zrghbao3oqkgj62bmlebw52djvatsakwviwxa4lj6fmmh7usiefd67yjf5eybfknmv2j3fvx2lb3fkm66xyuhaskniy","GoT.mp4");

    }

}
